package io.novasama.substrate_sdk_android.runtime.extrinsic.signer

import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Extrinsic
import io.novasama.substrate_sdk_android.runtime.definitions.types.toHex
import io.novasama.substrate_sdk_android.runtime.definitions.types.toHexUntyped

interface SendableExtrinsic {

    val extrinsic: Extrinsic.Instance

    val signatureHex: String

    val extrinsicHex: String
}

fun SendableExtrinsic(
    runtime: RuntimeSnapshot,
    extrinsic: Extrinsic.Instance
): SendableExtrinsic {
    return RealSendableExtrinsic(runtime, extrinsic)
}

private class RealSendableExtrinsic(
    private val runtime: RuntimeSnapshot,
    override val extrinsic: Extrinsic.Instance
) : SendableExtrinsic {

    override val extrinsicHex by lazy {
        createExtrinsicHex()
    }

    override val signatureHex by lazy {
        createSignatureHex()
    }

    private fun createExtrinsicHex(): String {
        return Extrinsic.toHex(runtime, extrinsic)
    }

    private fun createSignatureHex(): String {
        requireNotNull(extrinsic.signature) {
            "Extrinsic is unsigned"
        }

        val signatureType = Extrinsic.signatureType(runtime)

        return signatureType.toHexUntyped(runtime, extrinsic.signature.signature)
    }
}