package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.decoding

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.serializers.byteArraySerializer
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.internal.NamedValueDecoder
import kotlinx.serialization.modules.SerializersModule
import java.math.BigInteger

@OptIn(ExperimentalSerializationApi::class)
sealed interface ScaleDecoder {

    fun decodeByteArray(): ByteArray

    fun decodeNumber(): BigInteger

    fun decodeAsIs(): Any?
}

private const val ROOT_TAG = "ROOT"

@OptIn(ExperimentalSerializationApi::class)
open class SingleValueDecoder(
    serializersModule: SerializersModule,
    private val value: Any?,
) : BaseCompositeDecoder(serializersModule, value) {

    var elementDecoded = false

    init {
        pushTag(ROOT_TAG)
    }

    // to prevent failing requirement in putElement for nested context
    override fun composeName(parentName: String, childName: String) = parentName

    override fun getElement(tag: String): Any? {
        require(tag === ROOT_TAG)
        return value
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (elementDecoded) return CompositeDecoder.DECODE_DONE

        elementDecoded = true

        return 0
    }
}

inline fun <reified R> Any?.cast(): R = this as? R
    ?: throw IllegalArgumentException("Expected ${R::class.qualifiedName}, got ${this?.let { it::class.qualifiedName }}")

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
abstract class BaseCompositeDecoder(
    override val serializersModule: SerializersModule,
    private val value: Any?
) : NamedValueDecoder(), ScaleDecoder {

    protected abstract fun getElement(tag: String): Any?

    private fun currentObject() = currentTagOrNull?.let { getElement(it) } ?: value

    override fun decodeTaggedString(tag: String): String = getElement(tag).cast()
    override fun decodeTaggedBoolean(tag: String): Boolean = getElement(tag).cast()
    override fun decodeNumber(): BigInteger = getElement(popTag()).cast()
    override fun decodeByteArray(): ByteArray = getElement(popTag()).cast()

    override fun decodeAsIs() = getElement(popTag())

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {

        return when (descriptor.kind) {
            StructureKind.LIST -> CollectionDecoder(serializersModule, currentObject().cast())
            StructureKind.CLASS -> StructDecoder(serializersModule, currentObject().cast())
            StructureKind.OBJECT -> this
            else -> throw IllegalArgumentException("Unknown structure kind: ${descriptor.kind}")
        }
    }

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        return if (deserializer.descriptor == byteArraySerializer.descriptor) {
            decodeByteArray() as T
        } else {
            decodePolymorphically(currentObject().cast(), deserializer)
        }
    }
}

@ExperimentalSerializationApi
class EnumEncoder(
    serializersModule: SerializersModule,
    private val variantValue: Any?,
) : SingleValueDecoder(serializersModule, variantValue) {

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (descriptor.kind is StructureKind.CLASS && descriptor.elementsCount == 1) {
            return SingleValueDecoder(serializersModule, variantValue)
        }

        return super.beginStructure(descriptor)
    }
}

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
class StructDecoder(
    serializersModule: SerializersModule,
    private val struct: Struct.Instance,
) : BaseCompositeDecoder(serializersModule, struct) {

    private var position = 0

    override fun getElement(tag: String): Any? {
        return struct[tag]
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (position < descriptor.elementsCount) {
            position++
            val index = position - 1
            return index
        }
        return CompositeDecoder.DECODE_DONE
    }
}

class CollectionDecoder(
    serializersModule: SerializersModule,
    private val value: List<Any?>
) : BaseCompositeDecoder(serializersModule, value) {
    private val size = value.size
    private var currentIndex = -1

    override fun getElement(tag: String): Any? {
        return value[tag.toInt()]
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (currentIndex < size - 1) {
            currentIndex++
            return currentIndex
        }
        return CompositeDecoder.DECODE_DONE
    }

    override fun elementName(desc: SerialDescriptor, index: Int): String = index.toString()
}