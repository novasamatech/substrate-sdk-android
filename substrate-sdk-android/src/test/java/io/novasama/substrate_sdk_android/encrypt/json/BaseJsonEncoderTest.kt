package io.novasama.substrate_sdk_android.encrypt.json

import com.google.gson.Gson
import io.novasama.substrate_sdk_android.common.TestAddressBytes
import io.novasama.substrate_sdk_android.common.TestGeneses
import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.ss58.SS58Encoder.toAddress
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals


abstract class BaseJsonEncoderTest {

    protected val testMnemonic =
        "fine engage seed popular upon round differ belt engage space author pet"

    protected val password = "12345"

    protected val name = "test"


    private val gson = Gson()

    private val decoder = JsonDecoder(gson)
    private val encoder = JsonEncoder(gson)

    protected fun testExternalCompatibility(testCase: ExternalCompatibilityTestCase) {
        val expectedDerivationData = testCase.derivationData
        val decodedExternal = decoder.decode(testCase.json, password)

        with(decodedExternal) {
            assertArrayEquals(expectedDerivationData.keypair.publicKey, keypair.publicKey)
            assertArrayEquals(expectedDerivationData.keypair.privateKey, keypair.privateKey)

            assertEquals(expectedDerivationData.encryption, multiChainEncryption)

            assertEquals(name, username)
        }
    }

    protected fun testSelfCompatibility(testCase: SelfCompatibilityTestCase) {
        val derivationData = testCase.derivationData
        val keypairExpected = derivationData.keypair

        val address = keypairExpected.publicKey.toAddress(TestAddressBytes.WESTEND)

        val json = encoder.generate(
            keypair = keypairExpected,
            password = password,
            name = name,
            multiChainEncryption = derivationData.encryption,
            address = address,
            genesisHash = TestGeneses.WESTEND
        )

        val decoded = decoder.decode(json, password)

        with(decoded) {
            assertArrayEquals(keypairExpected.publicKey, keypair.publicKey)
            assertArrayEquals(keypairExpected.privateKey, keypair.privateKey)

            assertEquals(derivationData.encryption, multiChainEncryption)

            assertEquals(name, username)
        }
    }

    protected data class DerivationData(
        val keypair: Keypair,
        val encryption: MultiChainEncryption,
        val address: String
    )

    protected data class SelfCompatibilityTestCase(
        val derivationData: DerivationData,
    )

    protected data class ExternalCompatibilityTestCase(
        val derivationData: DerivationData,
        val json: String,
        val password: String,
    )
}