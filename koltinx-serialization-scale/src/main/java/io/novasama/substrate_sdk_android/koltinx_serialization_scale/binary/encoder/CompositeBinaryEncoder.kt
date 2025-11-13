package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.encoder

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.extensions.toSignedBytes
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.ElementDeclarationContext
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.ScaleOptional.NOT_NULL_MARK
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.ScaleOptional.NULL_MARK
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.encodeOptionalBoolean
import io.novasama.substrate_sdk_android.scale.utils.directWrite
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import java.nio.ByteOrder

internal class CompositeBinaryEncoder(
    override val serializersModule: SerializersModule,
    private val writer: ScaleCodecWriter,
) : CompositeEncoder {

    override fun endStructure(descriptor: SerialDescriptor) {}

    override fun encodeBooleanElement(
        descriptor: SerialDescriptor,
        index: Int,
        value: Boolean
    ) {
        ScaleCodecWriter.BOOL.write(writer, value)
    }

    override fun encodeByteElement(
        descriptor: SerialDescriptor,
        index: Int,
        value: Byte
    ) {
        writer.writeByte(value)
    }

    override fun encodeShortElement(
        descriptor: SerialDescriptor,
        index: Int,
        value: Short
    ) {
        writer.writeShort(value)
    }

    override fun encodeCharElement(
        descriptor: SerialDescriptor,
        index: Int,
        value: Char
    ) {
        unsupported("Char")
    }

    override fun encodeIntElement(
        descriptor: SerialDescriptor,
        index: Int,
        value: Int
    ) {
        // TODO can be greatly optimized
        val bytes = value.toBigInteger().toSignedBytes(
            resultByteOrder = ByteOrder.LITTLE_ENDIAN,
            expectedBytesSize = 4
        )

        writer.directWrite(bytes)
    }

    override fun encodeLongElement(
        descriptor: SerialDescriptor,
        index: Int,
        value: Long
    ) {
        writer.writeLong(value)
    }

    override fun encodeFloatElement(
        descriptor: SerialDescriptor,
        index: Int,
        value: Float
    ) {
        unsupported("Float")
    }

    override fun encodeDoubleElement(
        descriptor: SerialDescriptor,
        index: Int,
        value: Double
    ) {
        unsupported("Double")
    }

    override fun encodeStringElement(
        descriptor: SerialDescriptor,
        index: Int,
        value: String
    ) {
        writer.writeString(value)
    }

    override fun encodeInlineElement(
        descriptor: SerialDescriptor,
        index: Int
    ): Encoder {
        val elementContext = ElementDeclarationContext(index, descriptor)
        return PrimitiveBinaryScaleEncoder(serializersModule, writer, elementContext)
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        val elementContext = ElementDeclarationContext(index, descriptor)
        val delegate = PrimitiveBinaryScaleEncoder(serializersModule, writer, elementContext)
        delegate.encodeSerializableValue(serializer, value)
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        when {
            serializer.descriptor.kind == PrimitiveKind.BOOLEAN -> {
                writer.encodeOptionalBoolean(value as Boolean?)
            }

            value == null -> {
                writer.writeByte(NULL_MARK)
            }

            else -> {
                writer.writeByte(NOT_NULL_MARK)
                encodeSerializableElement(descriptor, index, serializer, value)
            }
        }
    }

    private fun unsupported(label: String): Nothing {
        throw SerializationException("Encoding of $label is not supported")
    }
}
