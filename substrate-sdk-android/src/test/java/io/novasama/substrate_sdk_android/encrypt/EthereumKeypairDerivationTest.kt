package io.novasama.substrate_sdk_android.encrypt

import com.google.gson.Gson
import io.novasama.substrate_sdk_android.encrypt.keypair.ethereum.EthereumKeypairFactory
import io.novasama.substrate_sdk_android.extensions.toHexString
import io.novasama.substrate_sdk_android.getResourceReader
import io.novasama.substrate_sdk_android.encrypt.junction.BIP32JunctionDecoder
import io.novasama.substrate_sdk_android.encrypt.mnemonic.MnemonicTestCase
import io.novasama.substrate_sdk_android.encrypt.seed.ethereum.EthereumSeedFactory
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
                ?.let { BIP32JunctionDecoder.decode(testCase.path) }

            val result = EthereumSeedFactory.deriveSeed(testCase.mnemonic, derivationPath?.password)

            val actualKeypair = EthereumKeypairFactory.generate(
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