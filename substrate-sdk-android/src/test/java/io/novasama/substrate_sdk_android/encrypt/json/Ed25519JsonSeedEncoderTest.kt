package io.novasama.substrate_sdk_android.encrypt.json

import io.novasama.substrate_sdk_android.encrypt.EncryptionType

class Ed25519JsonSeedEncoderTest : BaseSubstrateJsonEncoderTest() {

    override val encryptionType = EncryptionType.ED25519
}