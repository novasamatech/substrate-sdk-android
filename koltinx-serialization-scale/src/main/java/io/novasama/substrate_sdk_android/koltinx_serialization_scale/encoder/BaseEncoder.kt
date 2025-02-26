@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SealedClassSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.findPolymorphicSerializer
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import java.math.BigInteger

abstract class BaseEncoder: ScaleEncoder {

    protected abstract fun encodeIdentity(value: Any?)

    override fun beginCollection(
        descriptor: SerialDescriptor,
        collectionSize: Int
    ): CompositeEncoder {
        return ListEncoder(serializersModule, collectionSize, nodeConsumer = ::encodeIdentity)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return when (val kind = descriptor.kind) {
            StructureKind.CLASS -> StructEncoder(serializersModule, nodeConsumer = ::encodeIdentity)
            StructureKind.OBJECT -> ObjectEncoder(serializersModule, nodeConsumer = ::encodeIdentity)
            PolymorphicKind.SEALED -> EnumEncoder(serializersModule, nodeConsumer = ::encodeIdentity)
            else -> error("Unsupported descriptor kind: $kind")
        }
    }

    override fun encodeNumber(number: BigInteger) = encodeIdentity(number)

    override fun encodeByteArray(bytes: ByteArray) = encodeIdentity(bytes)

    override fun encodeBoolean(value: Boolean) = encodeIdentity(value)

    override fun encodeByte(value: Byte) = encodeIdentity(value)

    override fun encodeChar(value: Char) = unsupportedEncoding("Char")

    override fun encodeDouble(value: Double) = unsupportedEncoding("Double")

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        TODO("Enum encoding")
    }

    override fun encodeFloat(value: Float) = unsupportedEncoding("Float")

    @ExperimentalSerializationApi
    override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder {
        return this
    }

    override fun encodeInt(value: Int) = encodeNumber(value.toBigInteger())

    override fun encodeLong(value: Long) = encodeNumber(value.toBigInteger())

    @ExperimentalSerializationApi
    override fun encodeNull() = encodeIdentity(null)

    override fun encodeShort(value: Short) = encodeNumber(value.toInt().toBigInteger())

    override fun encodeString(value: String) = encodeIdentity(value.encodeToByteArray())

    private fun unsupportedEncoding(type: String): Nothing {
        error("Encoding $type is not supported")
    }

//    @OptIn(InternalSerializationApi::class)
//    @Suppress("UNCHECKED_CAST")
//    internal fun <T> encodePolymorphic(
//        serializer: SerializationStrategy<T>,
//        value: T,
//    ) {
//        if (serializer !is AbstractPolymorphicSerializer<*>) {
//            serializer.serialize(this, value)
//            return
//        }
//
//        val casted = serializer as AbstractPolymorphicSerializer<Any>
//        val actualSerializer = casted.findPolymorphicSerializer(this, value as Any)
//
//        val encoder = if (serializer is SealedClassSerializer<*>) {
//            // serialize Sealed Classes as Enums
//            val variantName = qualifiedClassNameToSimple(actualSerializer.descriptor.serialName)
//            EnumEncoder(serializersModule, nodeConsumer = ::encodeIdentity, variantName)
//        } else {
//            this
//        }
//
//        actualSerializer.serialize(encoder, value)
//    }
}