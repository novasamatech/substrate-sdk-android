package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder

import kotlinx.serialization.encoding.Encoder
import java.math.BigInteger

interface ScaleEncoder : Encoder {

    fun encodeNumber(number: BigInteger)

    fun encodeByteArray(bytes: ByteArray)
}