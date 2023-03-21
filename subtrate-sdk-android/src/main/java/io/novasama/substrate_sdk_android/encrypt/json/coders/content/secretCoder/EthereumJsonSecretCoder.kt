package io.novasama.substrate_sdk_android.encrypt.json.coders.content.secretCoder

import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.json.coders.content.JsonContentDecoder
import io.novasama.substrate_sdk_android.encrypt.json.coders.content.JsonSecretCoder
import io.novasama.substrate_sdk_android.encrypt.keypair.BaseKeypair
import io.novasama.substrate_sdk_android.encrypt.keypair.ECDSAUtils
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.encrypt.keypair.derivePublicKey

object EthereumJsonSecretCoder : JsonSecretCoder {

    override fun encode(keypair: Keypair, seed: ByteArray?): List<ByteArray> {
        return listOf(keypair.privateKey, keypair.publicKey)
    }

    override fun decode(data: List<ByteArray>): JsonContentDecoder.SecretDecoder.DecodedSecret {
        require(data.size == 2) { "Unknown secret structure (size: ${data.size}" }

        val privateKey = data[0]

        return JsonContentDecoder.SecretDecoder.DecodedSecret(
            seed = null,
            multiChainEncryption = MultiChainEncryption.Ethereum,
            keypair = BaseKeypair(
                privateKey = privateKey,
                publicKey = ECDSAUtils.derivePublicKey(privateKey)
            )
        )
    }
}
