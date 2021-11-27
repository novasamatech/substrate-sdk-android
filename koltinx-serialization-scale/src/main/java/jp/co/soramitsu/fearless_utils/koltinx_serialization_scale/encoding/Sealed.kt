package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encoding

import kotlinx.serialization.*
import kotlinx.serialization.encoding.Encoder

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
@Suppress("UNCHECKED_CAST")
internal fun <T> Encoder.encodeSealed(
    serializer: SerializationStrategy<T>,
    value: T,
    maybeConsumer: AnyConsumer
) {
    if (serializer !is SealedClassSerializer<*>) {
        serializer.serialize(this, value)
        return
    }
    val casted = serializer as SealedClassSerializer<Any>
    val actualSerializer = casted.findPolymorphicSerializer(this, value as Any)
    val variantName = qualifiedClassNameToSimple(actualSerializer.descriptor.serialName)
    val encoder = EnumEncoder(serializersModule, maybeConsumer, variantName)

    actualSerializer.serialize(encoder, value)
}

private fun qualifiedClassNameToSimple(qualified: String): String {
    return qualified.split(".").last()
}
