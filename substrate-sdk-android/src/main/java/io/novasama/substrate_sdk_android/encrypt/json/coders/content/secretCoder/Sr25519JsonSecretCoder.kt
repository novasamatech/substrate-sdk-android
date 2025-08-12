package io.novasama.substrate_sdk_android.encrypt.json.coders.content.secretCoder

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.Sr25519
import io.novasama.substrate_sdk_android.encrypt.json.coders.content.JsonContentDecoder
import io.novasama.substrate_sdk_android.encrypt.json.coders.content.JsonSecretCoder
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.encrypt.keypair.substrate.Sr25519Keypair

object Sr25519JsonSecretCoder : JsonSecretCoder {

    override fun encode(keypair: Keypair): List<ByteArray> {
        require(keypair is Sr25519Keypair)

        val ed25519BytesSecret = Sr25519.toEd25519Bytes(keypair.privateKey + keypair.nonce)

        return listOf(ed25519BytesSecret, keypair.publicKey)
    }

    override fun decode(data: List<ByteArray>): JsonContentDecoder.SecretDecoder.DecodedSecret {
        require(data.size == 2) { "Unknown secret format. Size: ${data.size}." }

        val (privateKeyCompressed, publicKey) = data

        val privateAndNonce = Sr25519.fromEd25519Bytes(privateKeyCompressed)

        val keypair = Sr25519Keypair(
            privateKey = privateAndNonce.copyOfRange(0, 32),
            publicKey = publicKey,
            nonce = privateAndNonce.copyOfRange(32, 64)
        )

        requirePublicKeyMatch(publicKeyFromJson = data[1], derivedKeyPair = keypair)

        return JsonContentDecoder.SecretDecoder.DecodedSecret(
            multiChainEncryption = MultiChainEncryption.Substrate(EncryptionType.SR25519),
            keypair = keypair
        )
    }
}
