package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.internal.NamedValueEncoder
import kotlinx.serialization.modules.SerializersModule
import java.math.BigInteger

@OptIn(ExperimentalSerializationApi::class)
sealed interface ScaleEncoder {

    fun encodeNumber(number: BigInteger)
}

typealias AnyConsumer = (Any?) -> Unit


@OptIn(ExperimentalSerializationApi::class)
class RootEncoder(override val serializersModule: SerializersModule) : ScaleEncoder, AbstractEncoder() {

    var result: Any? = null

    override fun encodeNumber(number: BigInteger) {
        result = number
    }

    override fun encodeString(value: String) {
        result = value
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return StructEncoder(
            serializersModule = serializersModule,
            consumer = { result = it }
        )
    }
}

@OptIn(InternalSerializationApi::class)
class StructEncoder(
    override val serializersModule: SerializersModule,
    private val consumer: AnyConsumer
) : NamedValueEncoder(), ScaleEncoder {

    var result: MutableMap<String, Any?> = mutableMapOf()

    override fun encodeNumber(number: BigInteger) {
        val tag = popTag()

        result[tag] = number
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return StructEncoder(
            serializersModule = serializersModule,
            consumer = { result[popTag()] = it }
        )
    }

    override fun encodeTaggedString(tag: String, value: String) {
        result[tag] = value
    }

    override fun endEncode(descriptor: SerialDescriptor) {
        consumer(result)
    }
}
