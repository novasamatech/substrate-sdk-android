package io.novasama.substrate_sdk_android.encrypt.json.coders.content.secretCoder

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption.Ethereum.encryptionType
import io.novasama.substrate_sdk_android.encrypt.json.coders.content.JsonContentDecoder
import io.novasama.substrate_sdk_android.encrypt.json.coders.content.JsonSecretCoder
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair

internal abstract class OtherSubstrateJsonSecretCoder : JsonSecretCoder {

    abstract val encryptionType: EncryptionType

    abstract fun createKeypair(privateKey: ByteArray): Keypair

    override fun encode(keypair: Keypair): List<ByteArray> {
        return listOf(keypair.privateKey, keypair.publicKey)
    }

    override fun decode(data: List<ByteArray>): JsonContentDecoder.SecretDecoder.DecodedSecret {
        require(data.size == 2) { "Unknown secret structure (size: ${data.size}" }

        val privateKey = data[0]
        val derivedKeypair = createKeypair(privateKey)

        requirePublicKeyMatch(publicKeyFromJson = data[1], derivedKeyPair = derivedKeypair)

        return JsonContentDecoder.SecretDecoder.DecodedSecret(
            multiChainEncryption = MultiChainEncryption.Substrate(encryptionType),
            keypair = derivedKeypair
        )
    }
}
