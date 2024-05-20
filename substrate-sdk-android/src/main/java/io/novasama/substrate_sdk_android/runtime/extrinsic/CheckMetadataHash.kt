package io.novasama.substrate_sdk_android.runtime.extrinsic

import io.novasama.substrate_sdk_android.runtime.metadata.SignedExtensionValue

sealed class CheckMetadataHash {

    object Disabled : CheckMetadataHash()

    class Enabled(val hash: ByteArray) : CheckMetadataHash()
}

private const val MODE_DISABLED = 0
private const val MODE_ENABLED = 1

internal fun CheckMetadataHash.toSignedExtensionValue(): SignedExtensionValue {
    return when (this) {
        CheckMetadataHash.Disabled -> SignedExtensionValue(
            includedInSignature = MODE_DISABLED,
            includedInExtrinsic = null
        )
        is CheckMetadataHash.Enabled -> SignedExtensionValue(
            includedInExtrinsic = MODE_ENABLED,
            includedInSignature = hash
        )
    }
}
