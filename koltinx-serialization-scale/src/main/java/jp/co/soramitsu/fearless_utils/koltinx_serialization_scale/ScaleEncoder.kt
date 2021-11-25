package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import java.math.BigInteger

internal object BigIntegerSerializer : KSerializer<BigInteger> {
    override fun deserialize(decoder: Decoder): BigInteger {
        TODO("Not yet implemented")
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Number", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigInteger) {
        require(encoder is ScaleEncoder)

        encoder.encodeNumber(value)
    }
}

@OptIn(ExperimentalSerializationApi::class)
sealed class ScaleEncoder(override val serializersModule: SerializersModule) : AbstractEncoder() {

    abstract fun encodeNumber(number: BigInteger)
}

class RootEncoder(serializersModule: SerializersModule): ScaleEncoder(serializersModule) {

     var result: Any? = null

    override fun encodeNumber(number: BigInteger) {
       result = number
    }

    override fun encodeString(value: String) {
        result = value
    }
}
