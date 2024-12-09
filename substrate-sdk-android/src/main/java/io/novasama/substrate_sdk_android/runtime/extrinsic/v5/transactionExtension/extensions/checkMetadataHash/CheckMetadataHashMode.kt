package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.checkMetadataHash

sealed class CheckMetadataHashMode {

    object Disabled : CheckMetadataHashMode()

    class Enabled(val hash: ByteArray) : CheckMetadataHashMode()
}

