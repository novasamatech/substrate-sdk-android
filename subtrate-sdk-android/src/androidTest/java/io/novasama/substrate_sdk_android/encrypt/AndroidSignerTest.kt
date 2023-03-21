package io.novasama.substrate_sdk_android.encrypt

import io.novasama.substrate_sdk_android.TestData
import io.novasama.substrate_sdk_android.encrypt.keypair.substrate.SubstrateKeypairFactory
import org.junit.Test

class AndroidSignerTest {

    @Test
    fun shouldSignMessage() {
        val messageHex = "this is a message"

        val keypair = SubstrateKeypairFactory.generate(EncryptionType.SR25519, TestData.SEED_BYTES)

        val result = Signer.sign(MultiChainEncryption.Substrate(EncryptionType.SR25519), messageHex.toByteArray(), keypair)

        require(
            SignatureVerifier.verify(
                result,
                Signer.MessageHashing.SUBSTRATE,
                messageHex.toByteArray(),
                keypair.publicKey
            )
        )
    }
}