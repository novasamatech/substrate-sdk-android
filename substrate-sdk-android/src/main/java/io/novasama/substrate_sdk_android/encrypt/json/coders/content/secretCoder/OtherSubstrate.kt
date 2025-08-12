package io.novasama.substrate_sdk_android.encrypt.json.coders.content.secretCoder

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption.Ethereum.encryptionType
import io.novasama.substrate_sdk_android.encrypt.json.coders.content.JsonContentDecoder
import io.novasama.substrate_sdk_android.encrypt.json.coders.content.JsonSecretCoder
import io.novasama.substrate_sdk_android.encrypt.keypair.Ed25519Utils
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.encrypt.keypair.substrate.SubstrateKeypairFactory


internal abstract class OtherSubstrateJsonSecretCoder : JsonSecretCoder {

    abstract val encryptionType: EncryptionType

    abstract fun createKeypair(privateKey: ByteArray): Keypair

    override fun encode(keypair: Keypair, seed: ByteArray?): List<ByteArray> {
        requireNotNull(seed) { "Seed cannot be null" }

        return listOf(keypair.privateKey, keypair.publicKey)
    }

    override fun decode(data: List<ByteArray>): JsonContentDecoder.SecretDecoder.DecodedSecret {
        require(data.size == 2) { "Unknown secret structure (size: ${data.size}" }

        val privateKey = data[0]
        val derivedKeypair = createKeypair(privateKey)

        val expectedPublicKey = data[1]
        require(expectedPublicKey.contentEquals(derivedKeypair.publicKey)) {
            "Generated public key does not match derived one"
        }

        return JsonContentDecoder.SecretDecoder.DecodedSecret(
            seed = null,
            multiChainEncryption = MultiChainEncryption.Substrate(encryptionType),
            keypair = derivedKeypair
        )
    }
}
