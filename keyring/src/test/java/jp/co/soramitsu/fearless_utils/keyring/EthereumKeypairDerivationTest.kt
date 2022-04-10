package jp.co.soramitsu.fearless_utils.keyring

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.encrypt.mnemonic.MnemonicTestCase
import jp.co.soramitsu.fearless_utils.getResourceReader
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import org.junit.Assert
import org.junit.Test

class EthereumKeypairDerivationTest {

    val gson = Gson()

    @Test
    fun `should run tests from json`() {
        val testCases = gson.fromJson(
            getResourceReader("crypto/BIP32HDKD.json"),
            Array<MnemonicTestCase>::class.java
        )

        testCases.forEach { testCase ->
            val derivationPathRaw = testCase.path.ifEmpty { null }

            val derivationPath = derivationPathRaw
                ?.let { jp.co.soramitsu.fearless_utils.keyring.junction.BIP32JunctionDecoder.decode(testCase.path) }

            val result = jp.co.soramitsu.fearless_utils.keyring.seed.ethereum.EthereumSeedFactory.deriveSeed(testCase.mnemonic, derivationPath?.password)

            val actualKeypair = jp.co.soramitsu.fearless_utils.keyring.keypair.ethereum.EthereumKeypairFactory.generate(
                seed = result.seed,
                junctions = derivationPath?.junctions.orEmpty()
            )

            Assert.assertEquals(
                "Mnemonic=${testCase.mnemonic}, derivationPath=${testCase.path}",
                testCase.expectedPublicKey,
                actualKeypair.publicKey.toHexString(withPrefix = true)
            )
        }
    }
}
