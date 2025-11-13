@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decoder

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule

class StructDecoder(
    private val reader: ScaleCodecReader,
    override val serializersModule: SerializersModule,
) : BaseCompositeBinaryDecoder(reader) {

    override fun elementsCount(descriptor: SerialDescriptor): Int {
        return descriptor.elementsCount
    }
}
