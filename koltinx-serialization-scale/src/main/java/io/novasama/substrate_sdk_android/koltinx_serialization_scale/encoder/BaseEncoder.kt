@file:OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations.AsDictEnum
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations.AsTuple
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations.TransientStruct
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.isAnnotatedWith
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigInteger

abstract class BaseEncoder : ScaleEncoder {

    protected abstract fun encodeIdentity(value: Any?)

    override fun beginCollection(
        descriptor: SerialDescriptor,
        collectionSize: Int
    ): CompositeEncoder {
        return ListEncoder(serializersModule, collectionSize, nodeConsumer = ::encodeIdentity)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return when (val kind = descriptor.kind) {
            StructureKind.CLASS -> descriptor.createStructEncoder()
            StructureKind.OBJECT -> ObjectEncoder(
                serializersModule,
                nodeConsumer = ::encodeIdentity
            )

            else -> error("Unsupported descriptor kind: $kind")
        }
    }

    override fun encodeRaw(value: Any?) = encodeIdentity(value)

    override fun encodeNumber(number: BigInteger) = encodeIdentity(number)

    override fun encodeByteArray(bytes: ByteArray) = encodeIdentity(bytes)

    override fun encodeBoolean(value: Boolean) = encodeIdentity(value)

    override fun encodeChar(value: Char) = unsupportedEncoding("Char")

    override fun encodeDouble(value: Double) = unsupportedEncoding("Double")

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        val elementName = enumDescriptor.getElementName(index)

        if (enumDescriptor.isAnnotatedWith<AsDictEnum>()) {
            encodeIdentity(DictEnum.Entry(elementName, null))
        } else {
            encodeIdentity(elementName)
        }
    }

    override fun encodeFloat(value: Float) = unsupportedEncoding("Float")

    @ExperimentalSerializationApi
    override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder {
        return this
    }

    override fun encodeInt(value: Int) = encodeNumber(value.toBigInteger())

    override fun encodeLong(value: Long) = encodeNumber(value.toBigInteger())

    override fun encodeByte(value: Byte) = encodeIdentity(value.toInt().toBigInteger())

    @ExperimentalSerializationApi
    override fun encodeNull() = encodeIdentity(null)

    override fun encodeShort(value: Short) = encodeNumber(value.toInt().toBigInteger())

    override fun encodeString(value: String) = encodeIdentity(value.encodeToByteArray())

    private fun unsupportedEncoding(type: String): Nothing {
        error("Encoding $type is not supported")
    }

    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        encodePolymorphic(serializer, value, nodeConsumer = ::encodeIdentity)
    }

    private fun SerialDescriptor.createStructEncoder(): CompositeEncoder {
        return when {
            isAnnotatedWith<TransientStruct>() -> {
                require(elementsCount == 1) {
                    "Cannot use @TransientStruct annotation on a class with more than 1 field"
                }

                TransientStructEncoder(serializersModule, nodeConsumer = ::encodeIdentity)
            }

            isAnnotatedWith<AsTuple>() -> {
                StructAsTupleEncoder(serializersModule, nodeConsumer = ::encodeIdentity)
            }

            else -> StructEncoder(serializersModule, nodeConsumer = ::encodeIdentity)
        }
    }
}
