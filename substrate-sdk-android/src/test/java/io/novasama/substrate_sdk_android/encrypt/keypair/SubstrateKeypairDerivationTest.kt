package io.novasama.substrate_sdk_android.encrypt.keypair

import com.google.gson.Gson
import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import io.novasama.substrate_sdk_android.encrypt.keypair.substrate.SubstrateKeypairFactory
import io.novasama.substrate_sdk_android.extensions.toHexString
import io.novasama.substrate_sdk_android.getResourceReader
import io.novasama.substrate_sdk_android.encrypt.junction.SubstrateJunctionDecoder
import io.novasama.substrate_sdk_android.encrypt.keypair.model.MnemonicTestCase
import io.novasama.substrate_sdk_android.encrypt.seed.substrate.SubstrateSeedFactory
import org.junit.Assert
import org.junit.Test

class SubstrateKeypairDerivationTest {

    @Test
    fun `should pass ed25519 tests`() {
        performSpecTests("crypto/ed25519HDKD.json", EncryptionType.ED25519)
    }

    @Test
    fun `should pass ecdsa tests`() {
        performSpecTests("crypto/ecdsaHDKD.json", EncryptionType.ECDSA)
    }

    @Test
    fun shouldRunSr25519Tests() {
        performSpecTests("crypto/sr25519HDKD.json", EncryptionType.SR25519)
    }

    val gson = Gson()

    protected fun performSpecTests(
        filename: String,
        encryptionType: EncryptionType
    ) {
        val testCases = gson.fromJson(
            getResourceReader(filename),
            Array<MnemonicTestCase>::class.java
        )

        testCases.forEach { testCase ->
            val derivationPathRaw = testCase.path.ifEmpty { null }

            val derivationPath = derivationPathRaw
                ?.let { SubstrateJunctionDecoder.decode(testCase.path) }

            val result = SubstrateSeedFactory.deriveSeed(testCase.mnemonic, derivationPath?.password)

            val seed32 = result.seed.copyOf(newSize = 32)

            val actualKeypair = SubstrateKeypairFactory.generate(
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

