package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decoder

import kotlinx.serialization.encoding.Decoder

interface BinaryScaleDecoder: Decoder {

    fun decodeFixedSizeArray(size: Int): ByteArray
}