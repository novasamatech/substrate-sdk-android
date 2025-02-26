@file:OptIn(InternalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SealedClassSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.findPolymorphicSerializer

@Suppress("UNCHECKED_CAST")
internal fun <T> Encoder.encodePolymorphic(
    serializer: SerializationStrategy<T>,
    value: T,
    nodeConsumer: (DictEnum.Entry<*>) -> Unit,
) {
    if (serializer !is SealedClassSerializer<*>) {
        serializer.serialize(this, value)
        return
    }

    val casted = serializer as SealedClassSerializer<Any>
    val actualSerializer = casted.findPolymorphicSerializer(this, value as Any)

    val variantName = qualifiedClassNameToSimple(actualSerializer.descriptor.serialName)
    val encoder = EnumEncoder(serializersModule, variantName, nodeConsumer)
    actualSerializer.serialize(encoder, value)
}

private fun qualifiedClassNameToSimple(qualified: String): String {
    return qualified.split(".").last()
}