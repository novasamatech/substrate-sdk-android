package io.novasama.substrate_sdk_android.encrypt.keypair

import io.novasama.substrate_sdk_android.encrypt.junction.Junction

internal fun <K : Keypair> KeypairFactory<K>.generate(
    seed: ByteArray,
    junctions: List<Junction>
): K {
    val parentKeypair = deriveFromSeed(seed)

    return junctions.fold(parentKeypair) { currentKeyPair, junction ->
        deriveChild(currentKeyPair, junction)
    }
}
