package jp.co.soramitsu.fearless_utils.keyring.signing


import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.getOrThrow
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.RuntimeType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.MultiSignature
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.prepareForEncoding
import jp.co.soramitsu.fearless_utils.signing.SignerResult

private const val EXTRINSIC_SIGNATURE_TYPE = "ExtrinsicSignature"

object SignatureInstanceConstructor : RuntimeType.InstanceConstructor<SignerResult> {

    override fun constructInstance(typeRegistry: TypeRegistry, value: SignerResult): Any {
        return when (val type = typeRegistry.getOrThrow(EXTRINSIC_SIGNATURE_TYPE)) {
            is DictEnum -> { // MultiSignature
                require()

                MultiSignature(value.encryptionType, value.signature).prepareForEncoding()
            }
            is Struct -> { // EthereumSignature
                require(value.encryptionType == EncryptionType.ECDSA.rawName) {
                    "Only ecdsa can be used for ethereum signatures"
                }

                val fields = mapOf(
                    "r" to value.r,
                    "s" to value.s,
                    "v" to value.vByte.toInt().toBigInteger()
                )

                Struct.Instance(fields)
            }
            else -> throw UnsupportedOperationException("Unknown signature type: ${type.name}")
        }
    }
}
