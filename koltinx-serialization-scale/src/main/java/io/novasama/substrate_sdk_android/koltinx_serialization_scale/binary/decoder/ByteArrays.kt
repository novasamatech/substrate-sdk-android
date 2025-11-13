package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decoder

import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor

private val byteArraySerializer = ByteArraySerializer().descriptor

fun SerialDescriptor.isByteArrayDescriptor(): Boolean {
    return this === byteArraySerializer
}
