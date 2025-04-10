package io.novasama.substrate_sdk_android.runtime.extrinsic

private const val EXTENSIONS_VERSION_DEFAULT: Byte = 0

sealed class ExtrinsicVersion {

    object V4 : ExtrinsicVersion()

    class V5(
        val extensionVersion: Byte = EXTENSIONS_VERSION_DEFAULT,
    ) : ExtrinsicVersion()
}
