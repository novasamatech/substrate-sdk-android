package jp.co.soramitsu.fearless_utils.runtime.definitions.types.instances

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.SignatureWrapper
import jp.co.soramitsu.fearless_utils.encrypt.vByte
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.getOrThrow
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct

private const val EXTRINSIC_SIGNATURE_TYPE = "ExtrinsicSignature"

private val EncryptionType.multiSignatureName
    get() = rawName.capitalize()

object SignatureInstanceConstructor : Type.InstanceConstructor<SignatureWrapper> {

    override fun constructInstance(typeRegistry: TypeRegistry, value: SignatureWrapper): Any {
        val type = typeRegistry.getOrThrow(EXTRINSIC_SIGNATURE_TYPE)

        return when (type) {
            is DictEnum -> { // MultiSignature
                DictEnum.Entry(value.encryptionType.multiSignatureName, value.signature)
            }
            is Struct -> { // EthereumSignature
                require(value is SignatureWrapper.Ecdsa) {
                    "Cannot construct extrinsic signature from ${value::class.simpleName}"
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
