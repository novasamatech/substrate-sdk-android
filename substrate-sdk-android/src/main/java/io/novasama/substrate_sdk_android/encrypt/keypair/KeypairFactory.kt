package io.novasama.substrate_sdk_android.encrypt.keypair

import io.novasama.substrate_sdk_android.encrypt.junction.Junction

interface KeypairFactory<K : Keypair> {

    class SoftDerivationNotSupported : Exception()

    fun deriveFromSeed(seed: ByteArray): K

    fun deriveChild(parent: K, junction: Junction): K
}

fun <K : Keypair> KeypairFactory<K>.generate(
    seed: ByteArray,
    junctions: List<Junction>
): K {
    return deriveKeyPair(deriveFromSeed(seed), junctions)
}

internal fun <K : Keypair> KeypairFactory<K>.deriveKeyPair(
    keypair: K,
    junctions: List<Junction>
): K {
    return junctions.fold(keypair) { currentKeyPair, junction ->
        deriveChild(currentKeyPair, junction)
    }
}
