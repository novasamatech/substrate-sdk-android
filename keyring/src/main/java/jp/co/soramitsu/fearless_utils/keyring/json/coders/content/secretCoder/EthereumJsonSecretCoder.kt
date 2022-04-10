package jp.co.soramitsu.fearless_utils.keyring.json.coders.content.secretCoder

import jp.co.soramitsu.fearless_utils.address.asPublicKey
import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import jp.co.soramitsu.fearless_utils.keyring.MultiChainEncryption
import jp.co.soramitsu.fearless_utils.keyring.json.coders.content.JsonContentDecoder
import jp.co.soramitsu.fearless_utils.keyring.json.coders.content.JsonSecretCoder
import jp.co.soramitsu.fearless_utils.keyring.keypair.BaseKeypair
import jp.co.soramitsu.fearless_utils.keyring.keypair.ECDSAUtils
import jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair
import jp.co.soramitsu.fearless_utils.keyring.keypair.derivePublicKey

object EthereumJsonSecretCoder : JsonSecretCoder {

    override fun encode(keypair: Keypair, seed: ByteArray?): List<ByteArray> {
        return listOf(keypair.privateKey, keypair.publicKey.value)
    }

    override fun decode(data: List<ByteArray>): JsonContentDecoder.SecretDecoder.DecodedSecret {
        require(data.size == 2) { "Unknown secret structure (size: ${data.size}" }

        val privateKey = data[0]

        return JsonContentDecoder.SecretDecoder.DecodedSecret(
            seed = null,
            multiChainEncryption = MultiChainEncryption.Ethereum,
            keypair = BaseKeypair(
                privateKey = privateKey,
                publicKey = ECDSAUtils.derivePublicKey(privateKey).asPublicKey(),
                encryptionType = EncryptionType.ECDSA
            )
        )
    }
}
