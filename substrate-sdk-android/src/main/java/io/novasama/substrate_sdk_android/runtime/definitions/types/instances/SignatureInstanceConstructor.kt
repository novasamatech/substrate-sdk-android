package io.novasama.substrate_sdk_android.runtime.definitions.types.instances

import io.novasama.substrate_sdk_android.encrypt.SignatureWrapper
import io.novasama.substrate_sdk_android.encrypt.vByte
import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypeRegistry
import io.novasama.substrate_sdk_android.runtime.definitions.registry.getOrThrow
import io.novasama.substrate_sdk_android.runtime.definitions.types.RuntimeType
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.MultiSignature
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.prepareForEncoding
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.FixedByteArray
import io.novasama.substrate_sdk_android.runtime.definitions.types.skipAliases

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
