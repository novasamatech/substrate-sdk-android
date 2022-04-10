package jp.co.soramitsu.fearless_utils.encrypt

import jp.co.soramitsu.fearless_utils.TestData
import jp.co.soramitsu.fearless_utils.keyring.keypair.substrate.SubstrateKeypairFactory
import org.junit.Test

class AndroidSignerTest {

    @Test
    fun shouldSignMessage() {
        val messageHex = "this is a message"

        val keypair = jp.co.soramitsu.fearless_utils.keyring.keypair.substrate.SubstrateKeypairFactory.generate(
            jp.co.soramitsu.fearless_utils.keyring.EncryptionType.SR25519, TestData.SEED_BYTES)

        val result = jp.co.soramitsu.fearless_utils.keyring.Signer.sign(
            jp.co.soramitsu.fearless_utils.keyring.MultiChainEncryption.Substrate(
                jp.co.soramitsu.fearless_utils.keyring.EncryptionType.SR25519), messageHex.toByteArray(), keypair)

        require(
            jp.co.soramitsu.fearless_utils.keyring.Signer.verifySr25519(
                messageHex.toByteArray(),
                result.signature,
                keypair.publicKey
            )
        )
    }
}
