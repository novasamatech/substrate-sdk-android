package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decoder

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.modules.SerializersModule

class ObjectDecoder(override val serializersModule: SerializersModule) : BaseCompositeDecoder() {

    override fun decodeIdentity(descriptor: SerialDescriptor, index: Int): Any? {
        error("Object decoder should have nothing to decode")
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
       return DECODE_DONE
    }
}