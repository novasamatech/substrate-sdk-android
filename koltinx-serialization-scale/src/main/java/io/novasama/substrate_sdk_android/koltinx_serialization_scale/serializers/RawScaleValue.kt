package io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.decoder.ScaleDecoder
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder.ScaleEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias RawScaleValue = @Serializable(with = RawScaleSerializer::class) Any?

@JvmInline
@Serializable
value class AsRawScaleValue(val value: RawScaleValue)

class RawScaleSerializer : KSerializer<Any?> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ScaleRawValue", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Any?
    ) {
        require(encoder is ScaleEncoder)
        encoder.encodeRaw(value)
    }

    override fun deserialize(decoder: Decoder): Any? {
        require(decoder is ScaleDecoder)
        return decoder.decodeRaw()
    }
}