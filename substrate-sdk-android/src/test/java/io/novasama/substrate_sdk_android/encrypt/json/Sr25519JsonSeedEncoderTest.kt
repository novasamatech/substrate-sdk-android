package io.novasama.substrate_sdk_android.encrypt.json

import io.novasama.substrate_sdk_android.encrypt.EncryptionType

class Sr25519JsonSeedEncoderTest : BaseSubstrateJsonEncoderTest() {

    override val encryptionType = EncryptionType.SR25519
}