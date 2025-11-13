package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.encoder

import kotlinx.serialization.encoding.Encoder
import java.math.BigInteger

interface BinaryScaleEncoder : Encoder {

    fun encodeCompact(compact: BigInteger)
}