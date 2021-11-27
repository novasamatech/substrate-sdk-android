package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encoding

import kotlinx.serialization.*
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.internal.AbstractPolymorphicSerializer

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
@Suppress("UNCHECKED_CAST")
internal fun <T> Encoder.encodePolymorphically(
    serializer: SerializationStrategy<T>,
    value: T,
    maybeConsumer: AnyConsumer
) {
    if (serializer !is AbstractPolymorphicSerializer<*>) {
        serializer.serialize(this, value)
        return
    }
    val casted = serializer as AbstractPolymorphicSerializer<Any>
    val actualSerializer = casted.findPolymorphicSerializer(this, value as Any)

    val encoder = if (serializer is SealedClassSerializer<*>) {
        // serialize Sealed Classes as Enums
        val variantName = qualifiedClassNameToSimple(actualSerializer.descriptor.serialName)
        EnumEncoder(serializersModule, maybeConsumer, variantName)
    } else {
        this
    }

    actualSerializer.serialize(encoder, value)
}

private fun qualifiedClassNameToSimple(qualified: String): String {
    return qualified.split(".").last()
}
