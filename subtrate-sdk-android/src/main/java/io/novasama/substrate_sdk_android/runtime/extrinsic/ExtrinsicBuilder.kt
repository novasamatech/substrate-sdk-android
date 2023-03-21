package io.novasama.substrate_sdk_android.runtime.extrinsic

import io.novasama.substrate_sdk_android.encrypt.SignatureWrapper
import io.novasama.substrate_sdk_android.runtime.AccountId
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.RuntimeType
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.AdditionalExtras
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Era
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Extrinsic
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Extrinsic.EncodingInstance.CallRepresentation
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.ExtrinsicPayloadExtrasInstance
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericCall
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.SignedExtras
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.create
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.new
import io.novasama.substrate_sdk_android.runtime.definitions.types.instances.AddressInstanceConstructor
import io.novasama.substrate_sdk_android.runtime.definitions.types.instances.SignatureInstanceConstructor
import io.novasama.substrate_sdk_android.runtime.definitions.types.toHex
import io.novasama.substrate_sdk_android.runtime.definitions.types.toHexUntyped
import io.novasama.substrate_sdk_android.runtime.extrinsic.signer.Signer
import io.novasama.substrate_sdk_android.runtime.extrinsic.signer.SignerPayloadExtrinsic
import io.novasama.substrate_sdk_android.runtime.metadata.call
import io.novasama.substrate_sdk_android.runtime.metadata.module
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.chain.RuntimeVersion
import java.math.BigInteger

private val DEFAULT_TIP = BigInteger.ZERO

class SignedExtension(val name: String, val type: Type<*>)

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
    private val customSignedExtensions: Map<SignedExtension, Any?> = emptyMap(),
    private val addressInstanceConstructor: RuntimeType.InstanceConstructor<AccountId> = AddressInstanceConstructor,
    private val signatureConstructor: RuntimeType.InstanceConstructor<SignatureWrapper> = SignatureInstanceConstructor
) {

    private val calls = mutableListOf<GenericCall.Instance>()

    private val extrinsicType = Extrinsic.create(customSignedExtensions.keys)

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

        return extrinsicType.toHex(runtime, extrinsic)
    }

    private suspend fun buildSignature(
        callRepresentation: CallRepresentation
    ): String {
        val multiSignature = buildSignatureObject(callRepresentation)

        val signatureType = extrinsicType.signatureType(runtime)

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
            AdditionalExtras.BLOCK_HASH to blockHash,
            AdditionalExtras.GENESIS to genesisHash,
            AdditionalExtras.SPEC_VERSION to runtimeVersion.specVersion.toBigInteger(),
            AdditionalExtras.TX_VERSION to runtimeVersion.transactionVersion.toBigInteger()
        )

        val signerPayload = SignerPayloadExtrinsic(
            runtime = runtime,
            extrinsicType = extrinsicType,
            accountId = accountId,
            call = callRepresentation,
            signedExtras = signedExtrasInstance,
            additionalExtras = additionalExtrasInstance,
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
            SignedExtras.MORTALITY to era,
            SignedExtras.TIP to tip,
            SignedExtras.NONCE to nonce
        )

        val custom = customSignedExtensions.mapKeys { (extension, _) -> extension.name }

        return default + custom
    }

    private fun requireNotMixingBytesAndInstanceCalls() {
        require(calls.isEmpty()) {
            "Cannot mix instance and raw bytes calls"
        }
    }
}
