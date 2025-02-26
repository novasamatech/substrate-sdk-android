package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule

class ObjectEncoder(
    override val serializersModule: SerializersModule,
    nodeConsumer: (Any?) -> Unit
): BaseCompositeEncoder(nodeConsumer) {

    override fun encodeIdentity(descriptor: SerialDescriptor, index: Int, value: Any?) {
        error("encodeIdentity should not be called when encoding an object")
    }

    override fun getEncodedValue(): Any? {
        return null
    }
}