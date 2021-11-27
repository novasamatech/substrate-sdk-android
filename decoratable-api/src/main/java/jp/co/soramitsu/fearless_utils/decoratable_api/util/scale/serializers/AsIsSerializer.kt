package jp.co.soramitsu.fearless_utils.decoratable_api.util.scale.serializers

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.ScaleDecoder
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encoding.ScaleEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

open class AsIsSerializer<T: Any>(private val tClass: KClass<T>) : KSerializer<T> {

    override fun deserialize(decoder: Decoder): T {
        require(decoder is ScaleDecoder)

        val decoded = decoder.decodeAsIs()

        if (decoded?.javaClass != tClass.java) {
            throw IllegalArgumentException("Expected ${tClass.qualifiedName}, got ${decoded?.javaClass?.canonicalName}")
        }

        return decoded as T
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AsIs${tClass.qualifiedName}", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) {
        require(encoder is ScaleEncoder)

        encoder.encodeAny(value)
    }
}
