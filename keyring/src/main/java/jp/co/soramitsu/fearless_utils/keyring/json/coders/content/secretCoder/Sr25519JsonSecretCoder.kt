package jp.co.soramitsu.fearless_utils.keyring.json.coders.content.secretCoder

import jp.co.soramitsu.fearless_utils.address.asPublicKey
import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import jp.co.soramitsu.fearless_utils.keyring.MultiChainEncryption
import jp.co.soramitsu.fearless_utils.keyring.Sr25519
import jp.co.soramitsu.fearless_utils.keyring.json.coders.content.JsonContentDecoder
import jp.co.soramitsu.fearless_utils.keyring.json.coders.content.JsonSecretCoder
import jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair
import jp.co.soramitsu.fearless_utils.keyring.keypair.substrate.Sr25519Keypair

object Sr25519JsonSecretCoder : JsonSecretCoder {

    override fun encode(keypair: Keypair, seed: ByteArray?): List<ByteArray> {
        require(keypair is Sr25519Keypair)

        val ed25519BytesSecret = Sr25519.toEd25519Bytes(keypair.privateKey + keypair.nonce)

        return listOf(ed25519BytesSecret, keypair.publicKey.value)
    }

    override fun decode(data: List<ByteArray>): JsonContentDecoder.SecretDecoder.DecodedSecret {
        require(data.size == 2) { "Unknown secret format. Size: ${data.size}." }

        val (privateKeyCompressed, publicKey) = data

        val privateAndNonce = Sr25519.fromEd25519Bytes(privateKeyCompressed)

        val keypair = Sr25519Keypair(
            privateAndNonce.copyOfRange(0, 32),
            publicKey.asPublicKey(),
            privateAndNonce.copyOfRange(32, 64)
        )

        return JsonContentDecoder.SecretDecoder.DecodedSecret(
            seed = null,
            multiChainEncryption = MultiChainEncryption.Substrate(EncryptionType.SR25519),
            keypair = keypair
        )
    }
}
