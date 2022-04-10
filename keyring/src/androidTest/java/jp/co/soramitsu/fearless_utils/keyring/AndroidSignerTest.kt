package jp.co.soramitsu.fearless_utils.keyring

import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.co.soramitsu.fearless_utils.TestData
import jp.co.soramitsu.fearless_utils.keyring.keypair.substrate.SubstrateKeypairFactory
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidSignerTest {

    @Test
    fun shouldSignMessage() {
        val messageHex = "this is a message"

        val keypair = SubstrateKeypairFactory.generate(
            EncryptionType.SR25519, TestData.SEED_BYTES)

        val result = Signing.sign(MultiChainEncryption.Substrate(EncryptionType.SR25519), messageHex.toByteArray(), keypair)

        assert(
            Signing.verifySr25519(
                messageHex.toByteArray(),
                result.signature,
                keypair.publicKey
            )
        )
    }
}
