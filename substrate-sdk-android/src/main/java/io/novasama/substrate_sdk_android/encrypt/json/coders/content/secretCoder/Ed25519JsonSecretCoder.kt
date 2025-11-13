package io.novasama.substrate_sdk_android.encrypt.json.coders.content.secretCoder

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import io.novasama.substrate_sdk_android.encrypt.keypair.Ed25519Utils
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair

internal object Ed25519JsonSecretCoder : OtherSubstrateJsonSecretCoder() {

    override val encryptionType: EncryptionType = EncryptionType.ED25519

    override fun createKeypair(privateKey: ByteArray): Keypair {
        return Ed25519Utils.createKeypair(privateKey)
    }
}
