package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.shouldUseTransientStructInEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule

class EnumEncoder(
    override val serializersModule: SerializersModule,
    private val variantName: String,
    private val nodeConsumer: (DictEnum.Entry<*>) -> Unit,
) : BaseEncoder() {

    override fun encodeIdentity(value: Any?) {
        nodeConsumer(DictEnum.Entry(variantName, value))
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        if (descriptor.shouldUseTransientStructInEnum()) {
            return TransientStructEncoder(serializersModule, nodeConsumer = ::encodeIdentity)
        }

        return super.beginStructure(descriptor)
    }
}
