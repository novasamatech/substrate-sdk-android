package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

abstract class BaseCompositeEncoder(
    private val nodeConsumer: (Any?) -> Unit
) : CompositeEncoder {

    abstract fun encodeIdentity(descriptor: SerialDescriptor, index: Int, value: Any?)

    abstract fun getEncodedValue(): Any?

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        encodeIdentity(descriptor, index, value)
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        encodeIdentity(descriptor, index, value.toInt().toBigInteger())
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        unsupportedEncoding("Char")
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        unsupportedEncoding("Double")
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        unsupportedEncoding("Float")
    }

    @ExperimentalSerializationApi
    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        return InlineOptimizationEncoder(descriptor, index)
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        encodeIdentity(descriptor, index, value.toBigInteger())
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        encodeIdentity(descriptor, index, value.toBigInteger())
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        encodeIdentity(descriptor, index, value.toInt().toBigInteger())
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        if (value == null) {
            encodeIdentity(descriptor, index, null)
        } else {
            encodeSerializableElement(descriptor, index, serializer, value)
        }
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        val encoder = SingleValueEncoder(serializersModule)
        serializer.serialize(encoder, value)
        encodeIdentity(descriptor, index, encoder.getCurrent())
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        encodeIdentity(descriptor, index, value.encodeToByteArray())
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        nodeConsumer(getEncodedValue())
    }

    private fun unsupportedEncoding(type: String): Nothing {
        error("Encoding $type is not supported")
    }

    private inner class InlineOptimizationEncoder(
        private val descriptor: SerialDescriptor,
        private val index: Int,
    ) : BaseEncoder() {

        override val serializersModule: SerializersModule
            get() = this@BaseCompositeEncoder.serializersModule

        override fun encodeIdentity(value: Any?) {
            encodeIdentity(descriptor, index, value)
        }
    }
}