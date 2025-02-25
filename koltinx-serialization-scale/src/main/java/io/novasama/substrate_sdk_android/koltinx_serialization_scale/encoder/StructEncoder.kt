package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule

class StructEncoder(
    override val serializersModule: SerializersModule,
    nodeConsumer: (Any?) -> Unit
) : BaseCompositeEncoder(nodeConsumer) {

    private var current = mutableMapOf<String, Any?>()

    override fun encodeIdentity(descriptor: SerialDescriptor, index: Int, value: Any?) {
        val tag = descriptor.getElementName(index)
        current[tag] = value
    }

    override fun getEncodedValue(): Struct.Instance {
        return Struct.Instance(current)
    }
}