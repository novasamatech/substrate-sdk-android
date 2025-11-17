package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.ScaleOptional.NULL_MARK
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.ScaleOptional.OPTIONAL_FALSE
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.common.ScaleOptional.OPTIONAL_TRUE
import io.novasama.substrate_sdk_android.scale.dataType.byte
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor

internal object ScaleOptional {

    const val NULL_MARK: Byte = 0

    const val NOT_NULL_MARK: Byte = 1

    const val OPTIONAL_FALSE: Byte = 1
    const val OPTIONAL_TRUE: Byte = 2
}

internal fun SerialDescriptor.isOptionalBoolean(): Boolean {
    return kind == PrimitiveKind.BOOLEAN && isNullable
}

internal fun ScaleCodecWriter.encodeOptionalBoolean(boolean: Boolean?) {
    val byte = when (boolean) {
        null -> NULL_MARK
        true -> OPTIONAL_TRUE
        false -> OPTIONAL_FALSE
    }
    writeByte(byte)
}
