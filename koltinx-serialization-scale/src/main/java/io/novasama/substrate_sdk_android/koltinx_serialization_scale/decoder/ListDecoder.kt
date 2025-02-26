package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decoder

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.modules.SerializersModule

class ListDecoder(
    override val serializersModule: SerializersModule,
    private val value: List<Any?>
) : BaseCompositeDecoder() {

    private var currentIndex = 0

    override fun decodeIdentity(descriptor: SerialDescriptor, index: Int): Any? {
        return value[index]
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (currentIndex < value.size) {
            currentIndex.also { currentIndex++ }
        } else {
            DECODE_DONE
        }
    }
}