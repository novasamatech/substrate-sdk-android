package io.novasama.substrate_sdk_android.encrypt.json.coders.content.secretCoder

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import io.novasama.substrate_sdk_android.encrypt.keypair.ECDSAUtils
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.encrypt.keypair.deriveKeypair

internal object EcdsaJsonSecretCoder : OtherSubstrateJsonSecretCoder() {

    override val encryptionType: EncryptionType = EncryptionType.ECDSA

    override fun createKeypair(privateKey: ByteArray): Keypair {
        return ECDSAUtils.deriveKeypair(privateKey)
    }
}
