package io.novasama.substrate_sdk_android.encrypt.json.coders.content.secretCoder

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.json.coders.content.JsonContentDecoder
import io.novasama.substrate_sdk_android.encrypt.json.coders.content.JsonSecretCoder
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.encrypt.keypair.substrate.Sr25519Keypair

object Sr25519JsonSecretCoder : JsonSecretCoder {

    override fun encode(keypair: Keypair, seed: ByteArray?): List<ByteArray> {
        require(keypair is Sr25519Keypair)

        val ed25519BytesSecret = io.novasama.substrate_sdk_android.encrypt.Sr25519.toEd25519Bytes(keypair.privateKey + keypair.nonce)

        return listOf(ed25519BytesSecret, keypair.publicKey)
    }

    override fun decode(data: List<ByteArray>): JsonContentDecoder.SecretDecoder.DecodedSecret {
        require(data.size == 2) { "Unknown secret format. Size: ${data.size}." }

        val (privateKeyCompressed, publicKey) = data

        val privateAndNonce = io.novasama.substrate_sdk_android.encrypt.Sr25519.fromEd25519Bytes(privateKeyCompressed)

        val keypair = Sr25519Keypair(
            privateAndNonce.copyOfRange(0, 32),
            publicKey,
            privateAndNonce.copyOfRange(32, 64)
        )

        return JsonContentDecoder.SecretDecoder.DecodedSecret(
            seed = null,
            multiChainEncryption = MultiChainEncryption.Substrate(EncryptionType.SR25519),
            keypair = keypair
        )
    }
}
