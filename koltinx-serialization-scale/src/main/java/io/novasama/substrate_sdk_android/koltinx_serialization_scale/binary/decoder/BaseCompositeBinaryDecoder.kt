package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decoder

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.ElementDeclarationContext
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.ScaleOptional.NULL_MARK
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder

abstract class BaseCompositeBinaryDecoder(
    private val reader: ScaleCodecReader,
) : CompositeDecoder {

    private var currentIndex: Int = 0

    abstract fun elementsCount(descriptor: SerialDescriptor): Int

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (currentIndex < elementsCount(descriptor)) {
            currentIndex++
        } else {
            DECODE_DONE
        }
    }

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
        return reader.readBoolean()
    }

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
        return reader.readByte()
    }

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
        unsupportedDecoding("Char")
    }

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
        unsupportedDecoding("Double")
    }

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
        unsupportedDecoding("Float")
    }

    @ExperimentalSerializationApi
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        return PrimitiveBinaryScaleDecoder(serializersModule, reader, null)
    }

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
       return ScaleCodecReader.INT32.read(reader)
    }

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
        return reader.readLong()
    }

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? {
        val nullabilityByte = reader.readByte()
        if (nullabilityByte == NULL_MARK) return null

        val elementContext = ElementDeclarationContext(index, descriptor, nullabilityByte)
        val delegate = PrimitiveBinaryScaleDecoder(serializersModule, reader, elementContext)
        return delegate.decodeSerializableValue(deserializer)
    }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        val elementContext = ElementDeclarationContext(index, descriptor, null)
        val delegate = PrimitiveBinaryScaleDecoder(serializersModule, reader, elementContext)
        return delegate.decodeSerializableValue(deserializer)
    }

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
        return reader.readShort()
    }

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
        return reader.readString()
    }

    override fun endStructure(descriptor: SerialDescriptor) {}

    private fun unsupportedDecoding(type: String): Nothing {
        error("Decoding $type is not supported")
    }
}