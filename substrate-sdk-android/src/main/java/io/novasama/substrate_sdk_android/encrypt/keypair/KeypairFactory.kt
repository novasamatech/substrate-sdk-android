package io.novasama.substrate_sdk_android.encrypt.keypair

import io.novasama.substrate_sdk_android.encrypt.junction.Junction

internal interface KeypairFactory<K : Keypair> {

    class SoftDerivationNotSupported : Exception()

    fun deriveFromSeed(seed: ByteArray): K

    fun deriveChild(parent: K, junction: Junction): K
}
