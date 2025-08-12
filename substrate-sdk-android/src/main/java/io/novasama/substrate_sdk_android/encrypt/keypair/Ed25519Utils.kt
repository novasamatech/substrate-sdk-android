package io.novasama.substrate_sdk_android.encrypt.keypair

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters

object Ed25519Utils {

    fun derivePublicFromPrivate(privateKey: ByteArray): ByteArray {
        val ed25519PrivateKey = Ed25519PrivateKeyParameters(privateKey, 0)
        val ed25519PublicKey = ed25519PrivateKey.generatePublicKey()
        return ed25519PublicKey.encoded
    }

    fun createKeypair(privateKey: ByteArray): Keypair {
        val ed25519PrivateKey = Ed25519PrivateKeyParameters(privateKey, 0)

        return BaseKeypair(
            publicKey = ed25519PrivateKey.generatePublicKey().encoded,
            privateKey = ed25519PrivateKey.encoded
        )
    }
}
