package io.novasama.substrate_sdk_android.runtime.extrinsic

import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature.VerifySignatureMode

private const val EXTENSIONS_VERSION_DEFAULT: Byte = 0

sealed class ExtrinsicVersion {

    class V4(
        val verifySignatureMode: VerifySignatureMode,
    ): ExtrinsicVersion()

    class V5(
        val extensionVersion: Byte = EXTENSIONS_VERSION_DEFAULT,
    ) : ExtrinsicVersion()
}
