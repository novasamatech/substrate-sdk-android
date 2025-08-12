package io.novasama.substrate_sdk_android.encrypt.json

import com.google.gson.Gson
import io.novasama.substrate_sdk_android.common.TestAddressBytes
import io.novasama.substrate_sdk_android.common.TestGeneses
import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.ss58.SS58Encoder.toAddress
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

private const val PASSWORD = "12345"
private const val NAME = "test"

private const val TEST_MNEMONIC = "fine engage seed popular upon round differ belt engage space author pet"

@RunWith(MockitoJUnitRunner::class)
abstract class BaseJsonEncoderTest {
    private val gson = Gson()

    private val decoder = JsonSeedDecoder(gson)
    private val encoder = JsonSeedEncoder(gson)

    abstract fun deriveSeedAndKeypair(mnemonic: String, derivationPath: String?): DerivationResult

    @Test
    fun `encode should be compatible with decode without derivation path`() {
        performTest(derivationPath = null)
    }

    @Test
    fun `encode should be compatible with decode with derivation path`() {
        performTest(derivationPath = "//1//2")
    }

    private fun performTest(derivationPath: String?) {
        val derivationResult = deriveSeedAndKeypair(TEST_MNEMONIC, derivationPath)

        val seedExpected = derivationResult.seed
        val keypairExpected = derivationResult.keypair

        val address = keypairExpected.publicKey.toAddress(TestAddressBytes.WESTEND)

        val json = encoder.generate(
            keypair = keypairExpected,
            seed = seedExpected,
            password = PASSWORD,
            name = NAME,
            multiChainEncryption = derivationResult.encryption,
            address = address,
            genesisHash = TestGeneses.WESTEND
        )

        val decoded = decoder.decode(json, PASSWORD)

        with(decoded) {
            assertArrayEquals(keypairExpected.publicKey, keypair.publicKey)
            assertArrayEquals(keypairExpected.privateKey, keypair.privateKey)

            assertEquals(derivationResult.encryption, multiChainEncryption)

            assertEquals(NAME, username)

            seed?.let {
                assertArrayEquals(seedExpected, it)
            }
        }
    }

    class DerivationResult(
        val seed: ByteArray,
        val keypair: Keypair,
        val encryption: MultiChainEncryption
    )
}