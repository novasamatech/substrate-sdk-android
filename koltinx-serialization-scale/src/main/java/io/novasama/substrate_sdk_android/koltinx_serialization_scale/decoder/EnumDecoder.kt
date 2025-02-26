@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decoder

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.shouldUseTransientStructInEnum
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule

/**
 * This implementation is based on how [AbstractPolymorphicSerializer] encodes sealed values
 * In particular, it expects two calls to be done in a sequence:
 *
 * 1. `decodeStringElement(descriptor, index)`
 * 2. `decodeSerializableElement(descriptor, index, serializer)`
 *
 * So it first encodes variant name and then the variant value
 */
// class EnumDecoder(
//    override val serializersModule: SerializersModule,
//    private val enumEntry: DictEnum.Entry<*>
// ) : BaseCompositeDecoder() {
//
//    companion object {
//
//        // Discriminator and value
//        private const val TOTAL_FIELDS = 2
//    }
//
//    private var index = 0
//
//    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
//        val classDiscriminator = "${descriptor.serialName}.${enumEntry.name}"
//        return classDiscriminator
//    }
//
//    override fun decodeIdentity(descriptor: SerialDescriptor, index: Int): Any? {
//        return enumEntry.value
//    }
//
//    override fun createSerializableElementDecoder(value: Any?): PrimitiveDecoder {
//        return EnumInnerTransientStructDecoder(serializersModule, value)
//    }
//
//    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
//        return if (index < TOTAL_FIELDS) {
//            index.also { index++ }
//        } else {
//            DECODE_DONE
//        }
//    }
//
//    private class EnumInnerTransientStructDecoder(
//        serializersModule: SerializersModule,
//        value: Any?
//    ) : PrimitiveDecoder(serializersModule, value) {
//
//        override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
//            if (descriptor.shouldUseTransientStructInEnum()) {
//                return TransientStructDecoder(serializersModule, singleField = value)
//            }
//
//            return super.beginStructure(descriptor)
//        }
//    }
// }

class EnumDecoder(
    serializersModule: SerializersModule,
    value: Any?
) : PrimitiveDecoder(serializersModule, value) {

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (descriptor.shouldUseTransientStructInEnum()) {
            return TransientStructDecoder(serializersModule, singleField = value)
        }

        return super.beginStructure(descriptor)
    }
}
