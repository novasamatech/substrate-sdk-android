package jp.co.soramitsu.fearless_utils.encrypt

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.keyring.keypair.substrate.SubstrateKeypairFactory
import jp.co.soramitsu.fearless_utils.getResourceReader
import jp.co.soramitsu.fearless_utils.keyring.junction.SubstrateJunctionDecoder
import jp.co.soramitsu.fearless_utils.encrypt.mnemonic.MnemonicTestCase
import jp.co.soramitsu.fearless_utils.keyring.seed.substrate.SubstrateSeedFactory
import org.junit.Assert

abstract class SubstrateKeypairDerivationTest {

    val gson = Gson()

    protected fun performSpecTests(
        filename: String,
        encryptionType: jp.co.soramitsu.fearless_utils.keyring.EncryptionType
    ) {
        val testCases = gson.fromJson(
            getResourceReader(filename),
            Array<MnemonicTestCase>::class.java
        )

        testCases.forEach { testCase ->
            val derivationPathRaw = testCase.path.ifEmpty { null }

            val derivationPath = derivationPathRaw
                ?.let { jp.co.soramitsu.fearless_utils.keyring.junction.SubstrateJunctionDecoder.decode(testCase.path) }

            val result = jp.co.soramitsu.fearless_utils.keyring.seed.substrate.SubstrateSeedFactory.deriveSeed(testCase.mnemonic, derivationPath?.password)

            val seed32 = result.seed.copyOf(newSize = 32)

            val actualKeypair = jp.co.soramitsu.fearless_utils.keyring.keypair.substrate.SubstrateKeypairFactory.generate(
                seed = seed32,
                junctions = derivationPath?.junctions.orEmpty(),
                encryptionType = encryptionType
            )

            Assert.assertEquals(
                "Mnemonic=${testCase.mnemonic}, derivationPath=${testCase.path}",
                testCase.expectedPublicKey,
                actualKeypair.publicKey.toHexString(withPrefix = true)
            )
        }
    }
}

