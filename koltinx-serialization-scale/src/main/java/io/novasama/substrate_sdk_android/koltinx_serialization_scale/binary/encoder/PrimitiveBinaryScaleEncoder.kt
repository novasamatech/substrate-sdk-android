package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.encoder

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.extensions.toSignedBytes
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.ElementDeclarationContext
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.EnumIndex
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.FixedLength
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.ScaleOptional.NOT_NULL_MARK
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.ScaleOptional.NULL_MARK
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.ScaleOptional.OPTIONAL_FALSE
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.ScaleOptional.OPTIONAL_TRUE
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decoder.isByteArrayDescriptor
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.findElementAnnotation
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.findAnnotation
import io.novasama.substrate_sdk_android.scale.dataType.compactInt
import io.novasama.substrate_sdk_android.scale.utils.directWrite
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import java.math.BigInteger
import java.nio.ByteOrder

class PrimitiveBinaryScaleEncoder(
    override val serializersModule: SerializersModule,
    private val writer: ScaleCodecWriter,
    private val elementContext: ElementDeclarationContext?,
) : BinaryScaleEncoder {

    @ExperimentalSerializationApi
    override fun encodeNull() {
        writer.writeByte(NULL_MARK)
    }

    @ExperimentalSerializationApi
    override fun encodeNotNullMark() {
        writer.writeByte(NOT_NULL_MARK)
    }

    override fun encodeBoolean(value: Boolean) {
        ScaleCodecWriter.BOOL.write(writer, value)
    }

    override fun encodeCompact(compact: BigInteger) {
        compactInt.write(writer, compact)
    }

    override fun encodeByte(value: Byte) {
        writer.writeByte(value)
    }

    override fun encodeShort(value: Short) {
        writer.writeShort(value)
    }

    override fun encodeChar(value: Char) {
        unsupported("Char")
    }

    override fun encodeInt(value: Int) {
        // TODO can be greatly optimized
        val bytes = value.toBigInteger().toSignedBytes(
            resultByteOrder = ByteOrder.LITTLE_ENDIAN,
            expectedBytesSize = 4
        )

        writer.directWrite(bytes)
    }

    override fun encodeLong(value: Long) {
        writer.writeLong(value)
    }

    override fun encodeFloat(value: Float) {
        unsupported("Float")
    }

    override fun encodeDouble(value: Double) {
        unsupported("Double")
    }

    override fun encodeString(value: String) {
        writer.writeString(value)
    }

    override fun encodeEnum(
        enumDescriptor: SerialDescriptor,
        index: Int
    ) {
        val indexFromAnnotation = enumDescriptor.getElementAnnotations(index)
            .findAnnotation<EnumIndex>()
            ?.index
        val indexToWrite = indexFromAnnotation ?: index

        writer.writeByte(indexToWrite.toByte())
    }


    override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        return this
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        TODO("Not yet implemented")
    }

    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        return when {
            serializer.descriptor.isOptionalBoolean() -> encodeOptionalBoolean(value as Boolean?)
            serializer.descriptor.isByteArrayDescriptor() -> encodeByteArray(value as ByteArray)
            else -> super.encodeSerializableValue(serializer, value)
        }
    }

    private fun encodeByteArray(byteArray: ByteArray) {
        val fixedSize = elementContext?.findElementAnnotation<FixedLength>()?.length

        return if (fixedSize != null) {
            val actualSize = byteArray.size

            if (actualSize != fixedSize) {
                val msg = "Size mismatch. Specified in @FixedLength: $fixedSize. Got: $actualSize"
                throw SerializationException(msg)
            }

            writer.directWrite(byteArray)
        } else {
            writer.writeByteArray(byteArray)
        }
    }

    private fun encodeOptionalBoolean(value: Boolean?) {
        val byte = when (value) {
            null -> NULL_MARK
            true -> OPTIONAL_TRUE
            false -> OPTIONAL_FALSE
        }
        writer.writeByte(byte)
    }

    private fun SerialDescriptor.isOptionalBoolean(): Boolean {
        return kind == PrimitiveKind.BOOLEAN && isNullable
    }

    private fun unsupported(label: String): Nothing {
        throw SerializationException("Encoding of $label is not supported")
    }
}