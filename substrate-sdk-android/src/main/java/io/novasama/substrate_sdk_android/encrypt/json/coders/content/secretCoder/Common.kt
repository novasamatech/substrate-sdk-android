package io.novasama.substrate_sdk_android.encrypt.json.coders.content.secretCoder

import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair

fun requirePublicKeyMatch(publicKeyFromJson: ByteArray, derivedKeyPair: Keypair) {
    require(publicKeyFromJson.contentEquals(derivedKeyPair.publicKey)) {
        "Generated public key does not match derived one"
    }
}