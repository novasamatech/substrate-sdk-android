package io.novasama.substrate_sdk_android.runtime.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.scale.dataType.CollectionEnumType

class CollectionEnum(
    name: String,
    val elements: List<String>
) : Type<String>(name) {

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): String {
        return CollectionEnumType(elements).read(scaleCodecReader)
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: RuntimeSnapshot, value: String) {
        CollectionEnumType(elements).write(scaleCodecWriter, value)
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance in elements
    }

    operator fun get(key: Int): String = elements[key]

    override val isFullyResolved = true
}
