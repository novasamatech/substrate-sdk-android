package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.serializers

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.decoding.ScaleDecoder
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encoding.ScaleEncoder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigInteger

@OptIn(ExperimentalSerializationApi::class)
object BigIntegerSerializer : KSerializer<BigInteger> {

    override fun deserialize(decoder: Decoder): BigInteger {
        require(decoder is ScaleDecoder)

        return decoder.decodeNumber()
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Number", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigInteger) {
        require(encoder is ScaleEncoder)

        encoder.encodeNumber(value)
    }
}