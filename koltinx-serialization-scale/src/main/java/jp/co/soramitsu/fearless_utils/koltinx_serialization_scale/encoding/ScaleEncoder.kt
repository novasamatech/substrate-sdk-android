package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encoding

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.serializers.bitFlagsSerializer
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.serializers.byteArraySerializer
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.internal.NamedValueEncoder
import kotlinx.serialization.modules.SerializersModule
import java.math.BigInteger

@OptIn(ExperimentalSerializationApi::class)
sealed interface ScaleEncoder {

    fun encodeNumber(number: BigInteger)

    fun encodeByteArray(bytes: ByteArray)

    fun encodeAny(any: Any?)
}

typealias AnyConsumer = (Any?) -> Unit

private const val ROOT_TAG = "ROOT"

@OptIn(ExperimentalSerializationApi::class)
open class SingleValueEncoder(serializersModule: SerializersModule, consumer: AnyConsumer = {}) : BaseCompositeEncoder(serializersModule, consumer) {

    private var result: Any? = null

    // to prevent failing requirement in putElement for nested context
    override fun composeName(parentName: String, childName: String) = parentName

    init {
        pushTag(ROOT_TAG)
    }

    override fun putElement(element: Any?, tag: String) {
        require(tag == ROOT_TAG)

        result = element
    }

    override fun getCurrent(): Any? = result
}

@OptIn(ExperimentalSerializationApi::class)
class ObjectEncoder(override val serializersModule: SerializersModule, private val consumer: AnyConsumer) : AbstractEncoder() {

    override fun endStructure(descriptor: SerialDescriptor) {
        consumer.invoke(null)
    }
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
    override fun encodeByteArray(bytes: ByteArray) = putElement(bytes, popTag())
    override fun encodeAny(any: Any?) = putElement(any, popTag())

    override fun composeName(parentName: String, childName: String) = childName

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        val consumer: AnyConsumer = createConsumer()

        return when (descriptor.kind) {
            StructureKind.LIST -> CollectionEncoder(serializersModule, consumer)
            StructureKind.CLASS -> StructEncoder(serializersModule, consumer)
            StructureKind.OBJECT -> ObjectEncoder(serializersModule, consumer)
            else -> throw IllegalArgumentException("Unknown structure kind: ${descriptor.kind}")
        }
    }

    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        when (serializer.descriptor) {
            byteArraySerializer.descriptor, bitFlagsSerializer.descriptor -> encodeAny(value)
            else -> encodePolymorphically(serializer, value, createConsumer())
        }
    }

    fun result() = getCurrent()

    override fun endEncode(descriptor: SerialDescriptor) {
        consumer(result())
    }

    protected open fun createConsumer(): AnyConsumer = { putElement(it, popTag()) }
}

@ExperimentalSerializationApi
class EnumEncoder(
    serializersModule: SerializersModule,
    private val consumer: AnyConsumer,
    private val variantName: String
) : SingleValueEncoder(serializersModule, consumer) {

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        if (descriptor.kind is StructureKind.CLASS && descriptor.elementsCount == 1) {
            return SingleValueEncoder(serializersModule, createConsumer())
        }

        return super.beginStructure(descriptor)
    }

    override fun createConsumer(): AnyConsumer = {
        putElement(it, popTag())

        consumer(getCurrent())
    }

    override fun getCurrent(): Any {
        return DictEnum.Entry(variantName, super.getCurrent())
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
