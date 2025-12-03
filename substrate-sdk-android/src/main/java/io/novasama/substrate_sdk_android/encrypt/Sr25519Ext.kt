package io.novasama.substrate_sdk_android.encrypt

fun sr25519PublicKeyFromSeed(seed: ByteArray): ByteArray {
    val keyPair = Sr25519.keypairFromSeed(seed)
    return keyPair.copyOfRange(64, keyPair.size)
}