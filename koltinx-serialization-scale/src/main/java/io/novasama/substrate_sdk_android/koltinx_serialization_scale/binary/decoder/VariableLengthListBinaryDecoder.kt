package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decoder

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule

class VariableLengthListBinaryDecoder(
    private val reader: ScaleCodecReader,
    override val serializersModule: SerializersModule
) : BaseCompositeBinaryDecoder(reader) {

    private val size = reader.readCompactInt()

    override fun elementsCount(descriptor: SerialDescriptor): Int {
        return size
    }
}
