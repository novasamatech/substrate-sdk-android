package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decoder

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule

// This is needed because `findPolymorphicSerializerOrNull` only accepts `CompositeDecoder`
// whereas actually only using `serializersModule` under the hood
internal class StubCompositeDecoder(
    override val serializersModule: SerializersModule
) : BaseCompositeDecoder() {

    override fun decodeIdentity(descriptor: SerialDescriptor, index: Int): Any? {
        error("STUB")
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        error("STUB")
    }
}