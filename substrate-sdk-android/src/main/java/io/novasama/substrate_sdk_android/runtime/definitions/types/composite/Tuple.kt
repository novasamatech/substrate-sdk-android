package io.novasama.substrate_sdk_android.runtime.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.RuntimeType
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.skipAliases
import io.novasama.substrate_sdk_android.runtime.definitions.types.skipAliasesOrNull

class Tuple(name: String, val typeReferences: List<TypeReference>) : Type<List<*>>(name) {

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): List<*> {
        return typeReferences.map { it.requireValue().decode(scaleCodecReader, runtime) }
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: RuntimeSnapshot, value: List<*>) {
        typeReferences.zip(value).onEach { (type, value) ->
            type.requireValue().encodeUnsafe(scaleCodecWriter, runtime, value)
        }
    }

    override fun isValidInstance(instance: Any?): Boolean {
        if (instance !is List<*>) return false

        val zipped = typeReferences.zip(instance)

        return zipped.size == typeReferences.size && zipped.all { (type, possibleValue) ->
            type.requireValue().isValidInstance(possibleValue)
        }
    }

    operator fun get(index: Int): Type<*>? = typeReferences[index].skipAliasesOrNull()?.value

    inline operator fun <reified R> get(index: Int): R? = get(index) as? R

    override val isFullyResolved: Boolean
        get() = typeReferences.all { it.isResolved() }
}

fun RuntimeType<*, *>.isEmptyTuple(): Boolean {
    val asTuple = skipAliases() as? Tuple ?: return false

    return asTuple.typeReferences.isEmpty()
}
