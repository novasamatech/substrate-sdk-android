package io.novasama.substrate_sdk_android.runtime.extrinsic

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import io.novasama.substrate_sdk_android.runtime.metadata.SignedExtensionValue

sealed class CheckMetadataHash {

    object Disabled : CheckMetadataHash()

    class Enabled(val hash: ByteArray) : CheckMetadataHash()
}

internal fun CheckMetadataHash.toSignedExtensionValue(): SignedExtensionValue {
    return when (this) {
        CheckMetadataHash.Disabled -> SignedExtensionValue(
            includedInExtrinsic = modeStructOf(enabled = false),
            includedInSignature = null
        )
        is CheckMetadataHash.Enabled -> SignedExtensionValue(
            includedInExtrinsic = modeStructOf(enabled = true),
            includedInSignature = hash
        )
    }
}

private fun modeStructOf(enabled: Boolean): Struct.Instance {
    val mode = if (enabled) {
        DictEnum.Entry("Enabled", null)
    } else {
        DictEnum.Entry("Disabled", null)
    }

    return Struct.Instance(mapOf("mode" to mode))
}
