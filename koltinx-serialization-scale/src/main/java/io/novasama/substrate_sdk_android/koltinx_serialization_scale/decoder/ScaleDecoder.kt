package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decoder

import kotlinx.serialization.encoding.Decoder
import java.math.BigInteger

interface ScaleDecoder : Decoder {

    fun decodeByteArray(): ByteArray

    fun decodeNumber(): BigInteger
}