package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.internal.NamedValueEncoder
import kotlinx.serialization.modules.SerializersModule
import java.math.BigInteger

@OptIn(ExperimentalSerializationApi::class)
sealed interface ScaleEncoder {

    fun encodeNumber(number: BigInteger)
}

typealias AnyConsumer = (Any?) -> Unit

private const val ROOT_TAG = "ROOT"

@OptIn(ExperimentalSerializationApi::class)
class RootEncoder(serializersModule: SerializersModule) : BaseCompositeEncoder(serializersModule, consumer = {}) {

    var result: Any? = null

    init {
        pushTag(ROOT_TAG)
    }

    override fun putElement(element: Any?, tag: String) {
        require(tag == ROOT_TAG)

        result = element
    }

    override fun getCurrent(): Any? = result
}

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
abstract class BaseCompositeEncoder(
    override val serializersModule: SerializersModule,
    private val consumer: AnyConsumer
) : NamedValueEncoder(), ScaleEncoder {

    protected abstract fun putElement(element: Any?, tag: String)
    protected abstract fun getCurrent(): Any?

    override fun encodeTaggedString(tag: String, value: String) = putElement(value, tag)
    override fun encodeTaggedBoolean(tag: String, value: Boolean) = putElement(value, tag)
    override fun encodeNumber(number: BigInteger) = putElement(number, popTag())

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        val consumer: AnyConsumer = { putElement(it, popTag()) }

        val encoder = when (descriptor.kind) {
            StructureKind.LIST -> CollectionEncoder(serializersModule, consumer)
            StructureKind.CLASS -> StructEncoder(serializersModule, consumer)
            else -> throw IllegalArgumentException("Unknown structure kind: ${descriptor.kind}")
        }

        return encoder
    }

    override fun endEncode(descriptor: SerialDescriptor) {
        consumer(getCurrent())
    }
}

@OptIn(InternalSerializationApi::class)
class StructEncoder(
    serializersModule: SerializersModule,
    consumer: AnyConsumer
) : BaseCompositeEncoder(serializersModule, consumer) {

    private var result: MutableMap<String, Any?> = mutableMapOf()


    override fun putElement(element: Any?, tag: String) {
        result[tag] = element
    }

    override fun getCurrent() = Struct.Instance(result)
}

class CollectionEncoder(
    serializersModule: SerializersModule,
    consumer: AnyConsumer
) : BaseCompositeEncoder(serializersModule, consumer) {

    private var result: MutableList<Any?> = mutableListOf()

    override fun elementName(descriptor: SerialDescriptor, index: Int): String = index.toString()

    override fun putElement(element: Any?, tag: String) {
        result.add(tag.toInt(), element)
    }

    override fun getCurrent() = result
}
