package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
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

    override fun encodeNumber(number: BigInteger) = putElement(number)
    override fun encodeString(value: String) = putElement(value)
    override fun encodeBoolean(value: Boolean) = putElement(value)

    private fun putElement(value: Any?) {
        result = value
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return StructEncoder(
            serializersModule = serializersModule,
            consumer = ::putElement
        )
    }
}

@OptIn(InternalSerializationApi::class)
class StructEncoder(
    override val serializersModule: SerializersModule,
    private val consumer: AnyConsumer
) : NamedValueEncoder(), ScaleEncoder {

    var result: MutableMap<String, Any?> = mutableMapOf()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return StructEncoder(
            serializersModule = serializersModule,
            consumer = ::putElement
        )
    }

    override fun encodeTaggedString(tag: String, value: String) = putElement(value, tag)
    override fun encodeTaggedBoolean(tag: String, value: Boolean) = putElement(value, tag)
    override fun encodeNumber(number: BigInteger) = putElement(number)

    override fun endEncode(descriptor: SerialDescriptor) {
        consumer(Struct.Instance(result))
    }

    private fun putElement(element: Any?, tag: String = popTag()) {
        result[tag] = element
    }
}
