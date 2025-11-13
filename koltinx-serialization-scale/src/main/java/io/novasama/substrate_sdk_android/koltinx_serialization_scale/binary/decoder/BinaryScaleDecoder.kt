package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decoder

import kotlinx.serialization.encoding.Decoder
import java.math.BigInteger

interface BinaryScaleDecoder: Decoder {

    fun decodeFixedSizeArray(size: Int): ByteArray

    fun decodeCompact(): BigInteger
}