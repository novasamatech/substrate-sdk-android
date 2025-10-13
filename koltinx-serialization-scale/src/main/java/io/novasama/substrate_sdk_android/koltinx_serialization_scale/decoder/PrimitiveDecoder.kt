@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decoder

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations.AsTuple
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations.TransientStruct
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations.findSerializedFallback
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.isAnnotatedWith
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SealedClassSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeDecoder.Companion.UNKNOWN_NAME
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import java.math.BigInteger

@OptIn(InternalSerializationApi::class)
open class PrimitiveDecoder(
    override val serializersModule: SerializersModule,
    val value: Any?
) : ScaleDecoder {

    override fun decodeRaw(): Any? {
        return value
    }

    override fun decodeByteArray(): ByteArray {
        return decodeCasted()
    }

    override fun decodeNumber(): BigInteger {
        return decodeCasted()
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return when (val kind = descriptor.kind) {
            StructureKind.CLASS -> descriptor.createStructDecoder(value)
            StructureKind.LIST -> ListDecoder(serializersModule, value as List<*>)
            StructureKind.OBJECT -> ObjectDecoder(serializersModule)
            else -> error("Unsupported descriptor kind: $kind")
        }
    }

    override fun decodeBoolean(): Boolean = decodeCasted()

    override fun decodeByte(): Byte = decodeNumber().toByte()

    override fun decodeChar(): Char = unsupportedDecoding("Char")

    override fun decodeDouble(): Double = unsupportedDecoding("Char")

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val name = detectEnumEntryName()
        val index = enumDescriptor.getElementIndex(name)

        if (index != UNKNOWN_NAME) return index

        val fallback = enumDescriptor.findSerializedFallback() ?: return index
        return enumDescriptor.getElementIndex(fallback)
    }

    private fun detectEnumEntryName(): String {
        return when(value) {
            is String -> value
            is DictEnum.Entry<*> -> {
                require(value.value == null) {
                    "Regular enum cannot be decoded with present associated value: $value"
                }

                value.name
            }
            else -> error("Cannot extract enum entry name from: $value")
        }
    }

    override fun decodeFloat(): Float = unsupportedDecoding("Float")

    @ExperimentalSerializationApi
    override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder {
        return this
    }

    override fun decodeInt(): Int = decodeNumber().toInt()

    override fun decodeLong() = decodeNumber().toLong()

    @ExperimentalSerializationApi
    override fun decodeNotNullMark(): Boolean {
        return value != null
    }

    @ExperimentalSerializationApi
    override fun decodeNull(): Nothing? {
        return null
    }

    override fun decodeShort(): Short {
        return decodeNumber().toShort()
    }

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        return decodePolymorphic(deserializer)
    }

    override fun decodeString(): String {
        return when (val raw = decodeIdentity()) {
            is ByteArray -> raw.decodeToString()
            is String -> raw
            else -> error("Unsupported value when attepmting to decode String: $raw")
        }
    }

    private inline fun <reified T> decodeCasted(): T {
        return decodeIdentity() as T
    }

    private fun unsupportedDecoding(type: String): Nothing {
        error("Decoding $type is not supported")
    }

    private fun decodeIdentity(): Any? = value

    private fun <T> decodePolymorphic(serializer: DeserializationStrategy<T>): T {
        if (serializer !is SealedClassSerializer<*>) {
            return serializer.deserialize(this)
        }

        val enumEntry = value as DictEnum.Entry<*>

        val stubDecoder = StubCompositeDecoder()

        val variantClassName = createClassName(serializer.descriptor, enumEntry.name)

        val actualSerializer =
            serializer.findPolymorphicSerializerOrNull(stubDecoder, variantClassName)
                // @SerialName annotation completely changes the key, not just the class suffix.
                // So we also check for enum entry name directly to handle SerialName changes
                ?: serializer.findPolymorphicSerializerOrNull(stubDecoder, enumEntry.name)
                ?: serializer.findFallbackFromAnnotations()
                ?: error("Subtype $variantClassName not registered")

        val enumDecoder = PrimitiveDecoder(serializersModule, enumEntry.value)
        return actualSerializer.deserialize(enumDecoder) as T
    }

    private fun <T : Any> SealedClassSerializer<T>.findFallbackFromAnnotations(): DeserializationStrategy<out T>? {
        val fallback = descriptor.findSerializedFallback() ?: return null
        val fallbackClassName = createClassName(descriptor, fallback)

        return findPolymorphicSerializerOrNull(StubCompositeDecoder(), fallbackClassName)
            ?: error("Subtype $fallbackClassName specified as fallback via @FallbackAnnotation is not registered")
    }

    private fun createClassName(descriptor: SerialDescriptor, subclassName: String): String {
        return descriptor.serialName + ".$subclassName"
    }

    private fun SerialDescriptor.createStructDecoder(value: Any?): CompositeDecoder {
        return when {
            isAnnotatedWith<TransientStruct>() -> {
                require(elementsCount == 1) {
                    "Cannot use @TransientStruct annotation on a class with more than 1 field"
                }

                TransientStructDecoder(serializersModule, value)
            }

            isAnnotatedWith<AsTuple>() -> {
                StructAsTupleDecoder(serializersModule, value as List<*>)
            }

            else -> {
                StructDecoder(serializersModule, value as Struct.Instance)
            }
        }
    }

    // This is needed because `findPolymorphicSerializerOrNull` only accepts `CompositeDecoder`
    // whereas actually only using `serializersModule` under the hood
    private inner class StubCompositeDecoder : BaseCompositeDecoder() {

        override val serializersModule: SerializersModule
            get() = this@PrimitiveDecoder.serializersModule

        override fun decodeIdentity(descriptor: SerialDescriptor, index: Int): Any? {
            error("STUB")
        }

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            error("STUB")
        }
    }
}
