package io.novasama.substrate_sdk_android.encrypt.json.coders.content.secretCoder

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.json.coders.content.JsonContentDecoder
import io.novasama.substrate_sdk_android.encrypt.json.coders.content.JsonSecretCoder
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.encrypt.keypair.substrate.SubstrateKeypairFactory

internal object EcdsaJsonSecretCoder : OtherSubstrateJsonSecretCoder(EncryptionType.ECDSA)
internal object Ed25519JsonSecretCoder : OtherSubstrateJsonSecretCoder(EncryptionType.ED25519)

internal abstract class OtherSubstrateJsonSecretCoder(
    private val encryptionType: EncryptionType
) : JsonSecretCoder {

    override fun encode(keypair: Keypair, seed: ByteArray?): List<ByteArray> {
        requireNotNull(seed) { "Seed cannot be null" }

        return listOf(seed, keypair.publicKey)
    }

    override fun decode(data: List<ByteArray>): JsonContentDecoder.SecretDecoder.DecodedSecret {
        require(data.size == 2) { "Unknown secret structure (size: ${data.size}" }

        val seed = data[0].copyOfRange(0, 32) // crop to 32 bytes

        return JsonContentDecoder.SecretDecoder.DecodedSecret(
            seed = seed,
            multiChainEncryption = MultiChainEncryption.Substrate(encryptionType),
            keypair = SubstrateKeypairFactory.generate(encryptionType, seed)
        )
    }
}
