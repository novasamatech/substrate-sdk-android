package jp.co.soramitsu.fearless_utils.runtime.extrinsic

import jp.co.soramitsu.fearless_utils.encrypt.SignatureWrapper
import jp.co.soramitsu.fearless_utils.runtime.AccountId
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.RuntimeType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.DefaultSignedExtensions
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Era
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Extrinsic
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Extrinsic.EncodingInstance.CallRepresentation
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.ExtrinsicPayloadExtrasInstance
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericCall
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.new
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.instances.AddressInstanceConstructor
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.instances.SignatureInstanceConstructor
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.toHex
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.toHexUntyped
import jp.co.soramitsu.fearless_utils.runtime.extrinsic.signer.Signer
import jp.co.soramitsu.fearless_utils.runtime.extrinsic.signer.SignerPayloadExtrinsic
import jp.co.soramitsu.fearless_utils.runtime.metadata.SignedExtensionId
import jp.co.soramitsu.fearless_utils.runtime.metadata.call
import jp.co.soramitsu.fearless_utils.runtime.metadata.module
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.chain.RuntimeVersion
import java.math.BigInteger

private val DEFAULT_TIP = BigInteger.ZERO

class ExtrinsicBuilder(
    val runtime: RuntimeSnapshot,
    private val nonce: BigInteger,
    private val runtimeVersion: RuntimeVersion,
    private val genesisHash: ByteArray,
    private val accountId: AccountId,
    private val signer: Signer,
    private val blockHash: ByteArray = genesisHash,
    private val era: Era = Era.Immortal,
    private val tip: BigInteger = DEFAULT_TIP,
    private val customSignedExtensions: Map<SignedExtensionId, Any?> = emptyMap(),
    private val addressInstanceConstructor: RuntimeType.InstanceConstructor<AccountId> = AddressInstanceConstructor,
    private val signatureConstructor: RuntimeType.InstanceConstructor<SignatureWrapper> = SignatureInstanceConstructor
) {

    private val calls = mutableListOf<GenericCall.Instance>()

    fun call(
        moduleIndex: Int,
        callIndex: Int,
        args: Map<String, Any?>
    ): ExtrinsicBuilder {
        val module = runtime.metadata.module(moduleIndex)
        val function = module.call(callIndex)

        calls.add(GenericCall.Instance(module, function, args))

        return this
    }

    fun call(
        moduleName: String,
        callName: String,
        arguments: Map<String, Any?>
    ): ExtrinsicBuilder {
        val module = runtime.metadata.module(moduleName)
        val function = module.call(callName)

        calls.add(GenericCall.Instance(module, function, arguments))

        return this
    }

    fun call(call: GenericCall.Instance): ExtrinsicBuilder {
        calls.add(call)

        return this
    }

    fun reset(): ExtrinsicBuilder {
        calls.clear()

        return this
    }

    suspend fun build(
        useBatchAll: Boolean = false
    ): String {
        val call = maybeWrapInBatch(useBatchAll)

        return build(CallRepresentation.Instance(call))
    }

    suspend fun build(
        rawCallBytes: ByteArray
    ): String {
        requireNotMixingBytesAndInstanceCalls()

        return build(CallRepresentation.Bytes(rawCallBytes))
    }

    suspend fun buildSignature(
        useBatchAll: Boolean = false
    ): String {
        val call = maybeWrapInBatch(useBatchAll)

        return buildSignature(CallRepresentation.Instance(call))
    }

    suspend fun buildSignature(
        rawCallBytes: ByteArray
    ): String {
        requireNotMixingBytesAndInstanceCalls()

        return buildSignature(CallRepresentation.Bytes(rawCallBytes))
    }

    private suspend fun build(
        callRepresentation: CallRepresentation
    ): String {
        val multiSignature = buildSignatureObject(callRepresentation)
        val signedExtras = buildSignedExtras()

        val extrinsic = Extrinsic.EncodingInstance(
            signature = Extrinsic.Signature.new(
                accountIdentifier = buildEncodableAddressInstance(),
                signature = multiSignature,
                signedExtras = signedExtras
            ),
            callRepresentation = callRepresentation
        )

        return Extrinsic.toHex(runtime, extrinsic)
    }

    private suspend fun buildSignature(
        callRepresentation: CallRepresentation
    ): String {
        val multiSignature = buildSignatureObject(callRepresentation)

        val signatureType = Extrinsic.signatureType(runtime)

        return signatureType.toHexUntyped(runtime, multiSignature)
    }

    private fun maybeWrapInBatch(useBatchAll: Boolean): GenericCall.Instance {
        return if (calls.size == 1) {
            calls.first()
        } else {
            wrapInBatch(useBatchAll)
        }
    }

    private suspend fun buildSignatureObject(callRepresentation: CallRepresentation): Any? {
        val signedExtrasInstance = buildSignedExtras()

        val additionalExtrasInstance = mapOf(
            DefaultSignedExtensions.CHECK_MORTALITY to blockHash,
            DefaultSignedExtensions.CHECK_GENESIS to genesisHash,
            DefaultSignedExtensions.CHECK_SPEC_VERSION to runtimeVersion.specVersion.toBigInteger(),
            DefaultSignedExtensions.CHECK_TX_VERSION to runtimeVersion.transactionVersion.toBigInteger()
        )

        val signerPayload = SignerPayloadExtrinsic(
            runtime = runtime,
            accountId = accountId,
            call = callRepresentation,
            signedExtras = signedExtrasInstance,
            additionalSignedExtras = additionalExtrasInstance,
        )
        val signatureWrapper = signer.signExtrinsic(signerPayload)

        return signatureConstructor.constructInstance(runtime.typeRegistry, signatureWrapper)
    }

    private fun wrapInBatch(useBatchAll: Boolean): GenericCall.Instance {
        val batchModule = runtime.metadata.module("Utility")
        val batchFunctionName = if (useBatchAll) "batch_all" else "batch"
        val batchFunction = batchModule.call(batchFunctionName)

        return GenericCall.Instance(
            module = batchModule,
            function = batchFunction,
            arguments = mapOf(
                "calls" to calls
            )
        )
    }

    private fun buildEncodableAddressInstance(): Any? {
        return addressInstanceConstructor.constructInstance(runtime.typeRegistry, accountId)
    }

    private fun buildSignedExtras(): ExtrinsicPayloadExtrasInstance {
        val default = mapOf(
            DefaultSignedExtensions.CHECK_MORTALITY to era,
            DefaultSignedExtensions.CHECK_TX_PAYMENT to tip,
            DefaultSignedExtensions.CHECK_NONCE to nonce
        )

        return default + customSignedExtensions
    }

    private fun requireNotMixingBytesAndInstanceCalls() {
        require(calls.isEmpty()) {
            "Cannot mix instance and raw bytes calls"
        }
    }
}
