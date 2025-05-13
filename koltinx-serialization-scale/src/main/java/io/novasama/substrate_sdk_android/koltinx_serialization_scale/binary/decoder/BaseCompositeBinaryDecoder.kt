package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decoder

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.ElementDeclarationContext
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
        TODO("Not yet implemented")
    }

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
        TODO("Not yet implemented")
    }

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
        TODO("Not yet implemented")
    }

    @ExperimentalSerializationApi
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        TODO("Not yet implemented")
    }

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
        TODO("Not yet implemented")
    }

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
        TODO("Not yet implemented")
    }

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? {
        TODO("Not yet implemented")
    }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        val elementContext = ElementDeclarationContext(index, descriptor)
        val delegate = PrimitiveBinaryScaleDecoder(serializersModule, reader, elementContext)
        return delegate.decodeSerializableValue(deserializer)
    }

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
        TODO("Not yet implemented")
    }

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
        TODO("Not yet implemented")
    }

    override fun endStructure(descriptor: SerialDescriptor) {}
}