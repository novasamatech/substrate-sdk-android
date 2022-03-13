package jp.co.soramitsu.fearless_utils.keyring.signing


import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import jp.co.soramitsu.fearless_utils.keyring.signing.extrinsic.multiSignatureName
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.getOrThrow
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.extrinsic.SignatureInstanceConstructor
import jp.co.soramitsu.fearless_utils.signing.MultiSignature

private const val EXTRINSIC_SIGNATURE_TYPE = "ExtrinsicSignature"

object DefaultSignatureInstanceConstructor : SignatureInstanceConstructor {

    override fun constructInstance(typeRegistry: TypeRegistry, value: MultiSignature): Any {
        return when (val type = typeRegistry.getOrThrow(EXTRINSIC_SIGNATURE_TYPE)) {
            is DictEnum -> { // MultiSignature
                DictEnum.Entry(value.encryptionType, value)
            }
            is Struct -> { // EthereumSignature
                require(value.encryptionType == EncryptionType.ECDSA.multiSignatureName) {
                    "Only ecdsa can be used for ethereum signatures"
                }
                require(value.signature.size == 65) {
                    "ECDSA signature should be 65 bytes long"
                }

                val fields = mapOf(
                    "r" to value.signature.copyOfRange(0, 32),
                    "s" to value.signature.copyOfRange(32, 64),
                    "v" to value.signature[64].toInt().toBigInteger()
                )

                Struct.Instance(fields)
            }
            else -> throw UnsupportedOperationException("Unknown signature type: ${type.name}")
        }
    }
}
