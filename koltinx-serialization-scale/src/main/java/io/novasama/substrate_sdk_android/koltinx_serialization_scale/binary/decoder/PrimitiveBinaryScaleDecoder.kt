@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decoder

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.ElementDeclarationContext
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.FixedLengthBytes
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.findElementAnnotation
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule

private const val NULL_MARK: Byte = 0
private const val OPTIONAL_FALSE: Byte = 1
private const val OPTIONAL_TRUE: Byte = 2

class PrimitiveBinaryScaleDecoder(
    override val serializersModule: SerializersModule,
    private val reader: ScaleCodecReader,
    private val elementContext: ElementDeclarationContext?,
) : BinaryScaleDecoder {

    private var nullabilityByte: Byte? = null

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
        return when(val data = nullabilityByte) {
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
        TODO("Not yet implemented")
    }

    override fun decodeFloat(): Float {
        unsupportedDecoding("Float")
    }

    @ExperimentalSerializationApi
    override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun decodeString(): String {
        TODO("Not yet implemented")
    }

    private fun unsupportedDecoding(type: String): Nothing {
        error("Decoding $type is not supported")
    }
}