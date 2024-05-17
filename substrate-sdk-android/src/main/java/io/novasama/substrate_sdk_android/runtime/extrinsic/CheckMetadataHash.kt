package io.novasama.substrate_sdk_android.runtime.extrinsic

sealed class CheckMetadataHash {

    object Disabled : CheckMetadataHash()

    class Enabled(val hash: ByteArray) : CheckMetadataHash()
}
