@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decoder

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.shouldUseTransientStructInEnum
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule

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
