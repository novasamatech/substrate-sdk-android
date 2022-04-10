package jp.co.soramitsu.fearless_utils.runtime.extrinsic

import jp.co.soramitsu.fearless_utils.hash.Hasher.blake2b256
import jp.co.soramitsu.fearless_utils.runtime.AccountId
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.RuntimeType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.AdditionalExtras
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Era
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Extrinsic
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Extrinsic.EncodingInstance.CallRepresentation
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.ExtrinsicPayloadExtrasInstance
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericCall
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.SignedExtras
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.create
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.new
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.instances.AddressInstanceConstructor
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.toHex
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.toHexUntyped
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.useScaleWriter
import jp.co.soramitsu.fearless_utils.runtime.metadata.call
import jp.co.soramitsu.fearless_utils.runtime.metadata.module
import jp.co.soramitsu.fearless_utils.scale.utils.directWrite
import jp.co.soramitsu.fearless_utils.signing.MultiSignature
import jp.co.soramitsu.fearless_utils.signing.Signer
import jp.co.soramitsu.fearless_utils.signing.SignerPayloadRaw
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.chain.RuntimeVersion
import java.math.BigInteger

typealias SignatureInstanceConstructor = RuntimeType.InstanceConstructor<MultiSignature>

class SignedExtension(val name: String, val type: Type<*>)

private val DEFAULT_TIP = BigInteger.ZERO
private const val PAYLOAD_HASH_THRESHOLD = 256

class ExtrinsicBuilder(
    val runtime: RuntimeSnapshot,
    private val nonce: BigInteger,
    private val runtimeVersion: RuntimeVersion,
    private val genesisHash: ByteArray,
    private val signer: Signer,
    private val origin: AccountId,
    private val signatureConstructor: SignatureInstanceConstructor,
    private val blockHash: ByteArray = genesisHash,
    private val era: Era = Era.Immortal,
    private val tip: BigInteger = DEFAULT_TIP,
    private val customSignedExtensions: Map<SignedExtension, Any?> = emptyMap(),
    private val addressConstructor: RuntimeType.InstanceConstructor<AccountId> = AddressInstanceConstructor
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

    fun build(
        useBatchAll: Boolean = false
    ): String {
        val call = maybeWrapInBatch(useBatchAll)

        return build(CallRepresentation.Instance(call))
    }

    fun build(
        rawCallBytes: ByteArray
    ): String {
        requireNotMixingBytesAndInstanceCalls()

        return build(CallRepresentation.Bytes(rawCallBytes))
    }

    fun buildSignature(
        useBatchAll: Boolean = false
    ): String {
        val call = maybeWrapInBatch(useBatchAll)

        return buildSignature(CallRepresentation.Instance(call))
    }

    fun buildSignature(
        rawCallBytes: ByteArray
    ): String {
        requireNotMixingBytesAndInstanceCalls()

        return buildSignature(CallRepresentation.Bytes(rawCallBytes))
    }

    private fun build(
        callRepresentation: CallRepresentation
    ): String {
        val multiSignature = buildSignatureObject(callRepresentation)
        val signedExtras = buildSignedExtras()

        val extrinsic = Extrinsic.EncodingInstance(
            signature = Extrinsic.Signature.new(
                accountIdentifier = addressConstructor.constructInstance(runtime.typeRegistry, origin),
                signature = multiSignature,
                signedExtras = signedExtras
            ),
            callRepresentation = callRepresentation
        )

        return extrinsicType.toHex(runtime, extrinsic)
    }

    private fun buildSignature(
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

    private fun buildSignatureObject(callRepresentation: CallRepresentation): Any? {
        val signedExtrasInstance = buildSignedExtras()

        val additionalExtrasInstance = mapOf(
            AdditionalExtras.BLOCK_HASH to blockHash,
            AdditionalExtras.GENESIS to genesisHash,
            AdditionalExtras.SPEC_VERSION to runtimeVersion.specVersion.toBigInteger(),
            AdditionalExtras.TX_VERSION to runtimeVersion.transactionVersion.toBigInteger()
        )

        val payloadBytes = useScaleWriter {
            when (callRepresentation) {
                is CallRepresentation.Instance ->
                    GenericCall.encode(this, runtime, callRepresentation.call)

                is CallRepresentation.Bytes ->
                    directWrite(callRepresentation.bytes)
            }

            extrinsicType.signedExtrasType.encode(this, runtime, signedExtrasInstance)
            AdditionalExtras.default.encode(this, runtime, additionalExtrasInstance)
        }

        val messageToSign = if (payloadBytes.size > PAYLOAD_HASH_THRESHOLD) {
            payloadBytes.blake2b256()
        } else {
            payloadBytes
        }

        val multiSignature = signer.signRaw(SignerPayloadRaw(messageToSign, origin))

        return signatureConstructor.constructInstance(runtime.typeRegistry, multiSignature)
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
