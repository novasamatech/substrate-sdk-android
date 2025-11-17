package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.encoder

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.extensions.toSignedBytes
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.ElementDeclarationContext
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.EnumIndex
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.FixedLength
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.ScaleOptional.NOT_NULL_MARK
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.ScaleOptional.NULL_MARK
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.encodeOptionalBoolean
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.isOptionalBoolean
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decoder.isByteArrayDescriptor
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.findElementAnnotation
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.findAnnotation
import io.novasama.substrate_sdk_android.scale.dataType.compactInt
import io.novasama.substrate_sdk_android.scale.utils.directWrite
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SealedClassSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.findPolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import java.math.BigInteger
import java.nio.ByteOrder

internal class PrimitiveBinaryScaleEncoder(
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
        return when (val kind = descriptor.kind) {
            StructureKind.CLASS,
            StructureKind.OBJECT,
                // For LIST, length is written in encodeSerializableValue - where we can access the list itself
                // to know its size
            StructureKind.LIST -> CompositeBinaryEncoder(serializersModule, writer)

            else -> error("Unsupported descriptor kind: $kind")
        }
    }

    private fun maybeWriteListLength(collectionSize: Int) {
        val fixedLength = elementContext?.findElementAnnotation<FixedLength>()?.length
        if (fixedLength != null) {
            checkFixedLengthSize(fixedLength, collectionSize)
        } else {
            writer.writeCompact(collectionSize)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @OptIn(InternalSerializationApi::class)
    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        val descriptor = serializer.descriptor

        return when {
            descriptor.isOptionalBoolean() -> writer.encodeOptionalBoolean(value as Boolean?)
            descriptor.isByteArrayDescriptor() -> encodeByteArray(value as ByteArray)
            descriptor.isList() -> {
                // Encode list size here since it is the last time we will have access to `value`
                maybeWriteListLength((value as Collection<*>).size)
                // And then delegate to default logic with `beginStructure`
                super.encodeSerializableValue(serializer, value)
            }

            serializer is SealedClassSerializer<*> -> encodePolymorphic(serializer as SealedClassSerializer<T & Any>, value as (T & Any))
            else -> super.encodeSerializableValue(serializer, value)
        }
    }

    @OptIn(InternalSerializationApi::class)
    private fun <T : Any> encodePolymorphic(
        serializer: SealedClassSerializer<T>,
        value: T
    ) {
        val actualSerializer = serializer.findPolymorphicSerializer(this, value)
        val index = actualSerializer.descriptor.findAnnotation<EnumIndex>()?.index
            ?: throw SerializationException("@EnumIndex annotation is required for sealed hierarchies")

        writer.writeByte(index)

        actualSerializer.serialize(this, value)
    }

    private fun encodeByteArray(byteArray: ByteArray) {
        val fixedSize = elementContext?.findElementAnnotation<FixedLength>()?.length

        return if (fixedSize != null) {
            checkFixedLengthSize(fixedSize, byteArray.size)

            writer.directWrite(byteArray)
        } else {
            writer.writeByteArray(byteArray)
        }
    }

    private fun checkFixedLengthSize(sizeFromAnnotation: Int, actual: Int) {
        if (sizeFromAnnotation != actual) {
            val msg = "Size mismatch. Specified in @FixedLength: $sizeFromAnnotation. Got: $actual"
            throw SerializationException(msg)
        }
    }

    private fun SerialDescriptor.isList(): Boolean {
        return kind == StructureKind.LIST
    }

    private fun unsupported(label: String): Nothing {
        throw SerializationException("Encoding of $label is not supported")
    }
}