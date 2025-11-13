package io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.decoder.ScaleDecoder
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder.ScaleEncoder
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigInteger

typealias BigIntegerSerializable = @Contextual BigInteger

object BigIntegerSerializer : KSerializer<BigInteger> {

    override fun deserialize(decoder: Decoder): BigInteger {
        require(decoder is ScaleDecoder)

        return decoder.decodeNumber()
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigInteger", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigInteger) {
        require(encoder is ScaleEncoder)

        encoder.encodeNumber(value)
    }
}
