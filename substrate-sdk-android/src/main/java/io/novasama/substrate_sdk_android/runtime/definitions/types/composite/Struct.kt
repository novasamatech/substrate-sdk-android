package io.novasama.substrate_sdk_android.runtime.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.RuntimeType
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.skipAliases

@Suppress("UNCHECKED_CAST")
class Struct(
    name: String,
    val mapping: LinkedHashMap<String, TypeReference>
) : Type<Struct.Instance>(name) {

    class Instance(val mapping: Map<String, Any?>) {
        inline operator fun <reified R> get(key: String): R? = mapping[key] as? R
    }

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): Instance {
        val values = mapping.mapValues { (_, type) ->
            type.requireValue().decode(scaleCodecReader, runtime)
        }

        return Instance(values)
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: RuntimeSnapshot, value: Instance) {
        mapping.forEach { (name, type) ->
            type.requireValue().encodeUnsafe(scaleCodecWriter, runtime, value[name])
        }
    }

    override fun isValidInstance(instance: Any?): Boolean {
        if (instance !is Instance) return false

        return mapping.all { (key, child) ->
            child.requireValue().isValidInstance(instance[key])
        }
    }

    inline operator fun <reified R : Type<*>> get(key: String): R? = mapping[key]?.value?.skipAliases() as? R

    override val isFullyResolved: Boolean
        get() = mapping.all { (_, ref) -> ref.isResolved() }
}

fun RuntimeType<*, *>.isEmptyStruct(): Boolean {
    val asStruct = skipAliases() as? Struct ?: return false

    return asStruct.mapping.isEmpty()
}
