package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.serializers

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.ScaleEncoder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigInteger

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = BigInteger::class)
object BigIntegerSerializer : KSerializer<BigInteger> {
    override fun deserialize(decoder: Decoder): BigInteger {
        TODO("Not yet implemented")
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Number", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigInteger) {
        require(encoder is ScaleEncoder)

        encoder.encodeNumber(value)
    }
}
