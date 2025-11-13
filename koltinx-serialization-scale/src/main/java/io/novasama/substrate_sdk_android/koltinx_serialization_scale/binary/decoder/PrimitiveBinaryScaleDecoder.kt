@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decoder

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.ElementDeclarationContext
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.EnumIndex
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.FixedLengthBytes
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.ScaleOptional.NULL_MARK
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.ScaleOptional.OPTIONAL_FALSE
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.ScaleOptional.OPTIONAL_TRUE
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.findElementAnnotation
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.findAnnotation
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule

class PrimitiveBinaryScaleDecoder(
    override val serializersModule: SerializersModule,
    private val reader: ScaleCodecReader,
    private val elementContext: ElementDeclarationContext?,
) : BinaryScaleDecoder {

    private var nullabilityByte: Byte? = elementContext?.nullabilityByte

    override fun decodeFixedSizeArray(size: Int): ByteArray {
        return reader.readByteArray(size)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return when (val kind = descriptor.kind) {
            StructureKind.CLASS -> StructDecoder(reader, serializersModule)
            StructureKind.LIST -> ListDecoder(reader, serializersModule)
            else -> error("Unsupported descriptor kind: $kind")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        return when {
            deserializer.descriptor.isByteArrayDescriptor() -> decodeByteArray() as T
            else -> return super.decodeSerializableValue(deserializer)
        }
    }

    private fun decodeByteArray(): ByteArray {
        val fixedSize = elementContext?.findElementAnnotation<FixedLengthBytes>()?.length

        return if (fixedSize != null) {
            decodeFixedSizeArray(fixedSize)
        } else {
            reader.readByteArray()
        }
    }

    override fun decodeBoolean(): Boolean {
        // Option<Boolean> uses single byte encoding, so we check previously read mark
        return when (val data = nullabilityByte) {
            null -> reader.readBoolean()
            OPTIONAL_FALSE -> false
            OPTIONAL_TRUE -> true
            else -> error("Invalid value read for type `Boolean?`: $data")
        }
    }

    override fun decodeByte(): Byte {
        return reader.readByte()
    }

    override fun decodeChar(): Char {
        unsupportedDecoding("Char")
    }

    override fun decodeDouble(): Double {
        unsupportedDecoding("Double")
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val index = reader.readByte().toInt()

        if (enumDescriptor.enumElementUsesIndexDirectly(index)) {
            return index
        }

        return enumDescriptor.findRealEnumElementIndex(index)
            ?: index // Return index as the fallback so kotlinx serialization will throw readable error
    }

    private fun SerialDescriptor.enumElementUsesIndexDirectly(index: Int): Boolean {
        return try {
            val customEnumIndex = getElementAnnotations(index).findAnnotation<EnumIndex>()
                ?.index?.toInt()

            customEnumIndex == null || customEnumIndex == index
        } catch (_: IndexOutOfBoundsException) {
            false
        }
    }

    private fun SerialDescriptor.findRealEnumElementIndex(customIndex: Int): Int? {
        for (i in 0 until elementsCount) {
            val indexFromAnnotation = getElementAnnotations(i)
                .findAnnotation<EnumIndex>()
                ?.index
                ?.toInt()

            if (indexFromAnnotation == customIndex) return i
        }

        return null
    }

    override fun decodeFloat(): Float {
        unsupportedDecoding("Float")
    }

    @ExperimentalSerializationApi
    override fun decodeInline(descriptor: SerialDescriptor): Decoder {
        return this
    }

    override fun decodeInt(): Int {
        return ScaleCodecReader.INT32.read(reader)
    }

    override fun decodeLong(): Long {
        return reader.readLong()
    }

    @ExperimentalSerializationApi
    override fun decodeNotNullMark(): Boolean {
        nullabilityByte = reader.readByte()
        return nullabilityByte != NULL_MARK
    }

    @ExperimentalSerializationApi
    override fun decodeNull(): Nothing? {
        return null
    }

    override fun decodeShort(): Short {
        return reader.readShort()
    }

    override fun decodeString(): String {
        return reader.readString()
    }

    private fun unsupportedDecoding(type: String): Nothing {
        error("Decoding $type is not supported")
    }
}