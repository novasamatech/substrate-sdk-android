package io.novasama.substrate_sdk_android.runtime.extrinsic.signer

interface SendableExtrinsic {

    val signatureHex: String

    val extrinsicHex: String
}
