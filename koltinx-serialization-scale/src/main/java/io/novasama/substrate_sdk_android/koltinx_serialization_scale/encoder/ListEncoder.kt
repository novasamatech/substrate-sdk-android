package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule

class ListEncoder(
    override val serializersModule: SerializersModule,
    expectedSize: Int,
    nodeConsumer: (Any?) -> Unit
) : BaseCompositeEncoder(nodeConsumer) {

    private var current = ArrayList<Any?>(expectedSize)

    override fun encodeIdentity(descriptor: SerialDescriptor, index: Int, value: Any?) {
        current.add(index, value)
    }

    override fun getEncodedValue(): List<Any?> {
        return current
    }
}
