package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.decoding

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SealedClassSerializer
import kotlinx.serialization.findPolymorphicSerializer
import kotlinx.serialization.internal.AbstractPolymorphicSerializer

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
@Suppress("UNCHECKED_CAST")
internal fun <T> BaseCompositeDecoder.decodePolymorphically(
    value: Any?,
    serializer: DeserializationStrategy<T>,
): T {
    if (serializer !is AbstractPolymorphicSerializer<*>) {
        return serializer.deserialize(this)
    }

    val enumEntry = value.cast<DictEnum.Entry<*>>()
    val casted = serializer as AbstractPolymorphicSerializer<Any>
    val variantClassName = serializer.descriptor.serialName + ".${enumEntry.name}"
    val actualSerializer = casted.findPolymorphicSerializer(this, variantClassName)

    return if (serializer is SealedClassSerializer<*>) {
        // serialize Sealed Classes as Enums
        actualSerializer.deserialize(EnumEncoder(serializersModule, enumEntry.value)) as T
    } else {
        error("Not sealed class")
    }
}
