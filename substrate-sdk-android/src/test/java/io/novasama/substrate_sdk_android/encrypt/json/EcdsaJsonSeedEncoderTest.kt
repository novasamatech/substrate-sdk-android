package io.novasama.substrate_sdk_android.encrypt.json

import io.novasama.substrate_sdk_android.encrypt.EncryptionType

class EcdsaJsonSeedEncoderTest : BaseSubstrateJsonEncoderTest() {

    override val encryptionType = EncryptionType.ECDSA
}