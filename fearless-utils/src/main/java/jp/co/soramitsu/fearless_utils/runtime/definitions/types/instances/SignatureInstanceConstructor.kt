package jp.co.soramitsu.fearless_utils.runtime.definitions.types.instances

import jp.co.soramitsu.fearless_utils.encrypt.SignatureWrapper
import jp.co.soramitsu.fearless_utils.encrypt.vByte
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.getOrThrow
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.RuntimeType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.MultiSignature
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.prepareForEncoding
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.FixedByteArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.skipAliases

object SignatureInstanceConstructor : RuntimeType.InstanceConstructor<SignatureWrapper> {

    override fun constructInstance(typeRegistry: TypeRegistry, value: SignatureWrapper): Any {
        return when (val type = typeRegistry.getOrThrow(ExtrinsicTypes.SIGNATURE).skipAliases()) {
            is DictEnum -> { // MultiSignature
                MultiSignature(value.encryptionType, value.signature).prepareForEncoding()
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
            is FixedByteArray -> value.signature

            else -> throw UnsupportedOperationException("Unknown signature type: ${type?.name}")
        }
    }
}
