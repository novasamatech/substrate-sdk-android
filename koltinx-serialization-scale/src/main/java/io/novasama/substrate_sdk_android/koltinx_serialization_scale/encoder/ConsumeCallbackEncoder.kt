package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder

import kotlinx.serialization.modules.SerializersModule

class ConsumeCallbackEncoder(
    override val serializersModule: SerializersModule,
    private val nodeConsumer: (Any?) -> Unit
) : BaseEncoder() {

    override fun encodeIdentity(value: Any?) {
        nodeConsumer(value)
    }
}
