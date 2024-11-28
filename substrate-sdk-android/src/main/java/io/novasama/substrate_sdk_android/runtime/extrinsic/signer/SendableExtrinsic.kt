package io.novasama.substrate_sdk_android.runtime.extrinsic.signer

import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Extrinsic

interface SendableExtrinsic {

    val extrinsic: Extrinsic.EncodingInstance

    val signatureHex: String

    val extrinsicHex: String
}
