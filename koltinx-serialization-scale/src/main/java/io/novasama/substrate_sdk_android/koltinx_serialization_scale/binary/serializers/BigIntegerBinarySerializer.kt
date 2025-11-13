package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.serializers

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decoder.BinaryScaleDecoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigInteger

object BigIntegerBinarySerializer : KSerializer<BigInteger> {

    override fun deserialize(decoder: Decoder): BigInteger {
        require(decoder is BinaryScaleDecoder)

        return decoder.decodeCompact()
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigInteger", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigInteger) {
        TODO("Not yet implemented")
    }
}
