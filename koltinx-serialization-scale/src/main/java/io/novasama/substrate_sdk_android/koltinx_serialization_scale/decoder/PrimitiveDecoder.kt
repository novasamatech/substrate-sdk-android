@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decoder

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SealedClassSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import java.math.BigInteger

@OptIn(InternalSerializationApi::class)
open class PrimitiveDecoder(
    override val serializersModule: SerializersModule,
    val value: Any?
) : ScaleDecoder {

    override fun decodeByteArray(): ByteArray {
        return decodeCasted()
    }

    override fun decodeNumber(): BigInteger {
        return decodeCasted()
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return when (val kind = descriptor.kind) {
            StructureKind.CLASS -> StructDecoder(serializersModule, value as Struct.Instance)
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
        return enumDescriptor.getElementIndex(decodeString())
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
        return when(val raw = decodeIdentity()) {
            is ByteArray -> raw.decodeToString()
            is String -> raw
            else -> error("Unsupported value when attepmting to decode String: ${raw}")
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
        val variantClassName = serializer.descriptor.serialName + ".${enumEntry.name}"

        val actualSerializer =  serializer.findPolymorphicSerializerOrNull(StubCompositeDecoder(), variantClassName)
            ?: error("Subtype $variantClassName not registered")

        val enumDecoder = EnumDecoder(serializersModule, enumEntry.value)
        return actualSerializer.deserialize(enumDecoder) as T
    }

    // This is needed because `findPolymorphicSerializerOrNull` only accepts `CompositeDecoder`
    // whereas actually only using `serializersModule` under the hood
    private inner class StubCompositeDecoder: BaseCompositeDecoder() {

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