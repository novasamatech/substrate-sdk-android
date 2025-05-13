package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.serializers

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decoder.BinaryScaleDecoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias ScaleByteArray20 = @Serializable(ScaleFixedByteArray20Serializer::class) ByteArray
typealias FixedByteArray32 = @Serializable(ScaleFixedByteArray32Serializer::class) ByteArray
typealias ScaleByteArray64 = @Serializable(ScaleFixedByteArray64Serializer::class) ByteArray

class ScaleFixedByteArray20Serializer : ScaleFixedByteArraySerializer(20)
class ScaleFixedByteArray32Serializer : ScaleFixedByteArraySerializer(32)
class ScaleFixedByteArray64Serializer : ScaleFixedByteArraySerializer(64)

abstract class ScaleFixedByteArraySerializer(private val size: Int): KSerializer<ByteArray> {

    override fun deserialize(decoder: Decoder): ByteArray {
        require(decoder is BinaryScaleDecoder)

        return decoder.decodeFixedSizeArray(size)
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ScaleFixedByteArray${size}", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ByteArray) {
        TODO("not yet implemented")
    }
}