package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decoder

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import java.math.BigInteger

abstract class BaseCompositeDecoder : CompositeDecoder {

    abstract fun decodeIdentity(descriptor: SerialDescriptor, index: Int): Any?

    abstract override fun decodeElementIndex(descriptor: SerialDescriptor): Int

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
        return decodeCasted(descriptor, index)
    }

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
        return decodeNumber(descriptor, index).toByte()
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
        val value = decodeIdentity(descriptor, index)
        return PrimitiveDecoder(serializersModule, value)
    }

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
        return decodeNumber(descriptor, index).toInt()
    }

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
        return decodeNumber(descriptor, index).toLong()
    }

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? {
        val rawValue = decodeIdentity(descriptor, index) ?: return null
        return decodeSerializableValue(rawValue, deserializer)
    }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        val rawValue = decodeIdentity(descriptor, index)
        return decodeSerializableValue(rawValue, deserializer)
    }


    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
        return decodeNumber(descriptor, index).toShort()
    }

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
        return decodeByteArray(descriptor, index).decodeToString()
    }

    override fun endStructure(descriptor: SerialDescriptor) {}

    protected open fun createSerializableElementDecoder(value: Any?): PrimitiveDecoder {
        return PrimitiveDecoder(serializersModule, value)
    }

    private fun <T> decodeSerializableValue(
        rawValue: Any?,
        deserializer: DeserializationStrategy<T>,
    ): T {
        val decoder = createSerializableElementDecoder(rawValue)
        return decoder.decodeSerializableValue(deserializer)
    }

    private inline fun <reified T> decodeCasted(descriptor: SerialDescriptor, index: Int): T {
        return decodeIdentity(descriptor, index) as T
    }

    private fun decodeNumber(descriptor: SerialDescriptor, index: Int): BigInteger {
        return decodeCasted(descriptor, index)
    }

    private fun decodeByteArray(descriptor: SerialDescriptor, index: Int): ByteArray {
        return decodeCasted(descriptor, index)
    }

    private fun unsupportedDecoding(type: String): Nothing {
        error("Decoding $type is not supported")
    }
}