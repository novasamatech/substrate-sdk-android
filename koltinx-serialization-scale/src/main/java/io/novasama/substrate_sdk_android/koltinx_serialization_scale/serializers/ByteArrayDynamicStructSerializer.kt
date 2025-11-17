package io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.decoder.ScaleDecoder
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder.ScaleEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ByteArrayDynamicStructSerializer : KSerializer<ByteArray> {

    override fun deserialize(decoder: Decoder): ByteArray {
        require(decoder is ScaleDecoder)

        return decoder.decodeByteArray()
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ScaleByteArray", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ByteArray) {
        require(encoder is ScaleEncoder)

        encoder.encodeByteArray(value)
    }
}
