package io.novasama.substrate_sdk_android.encrypt

import com.google.gson.Gson
import io.novasama.substrate_sdk_android.TestData
import io.novasama.substrate_sdk_android.common.TestAddressBytes
import io.novasama.substrate_sdk_android.common.TestGeneses
import io.novasama.substrate_sdk_android.encrypt.json.JsonSeedDecoder
import io.novasama.substrate_sdk_android.encrypt.json.JsonSeedEncoder
import io.novasama.substrate_sdk_android.encrypt.keypair.substrate.SubstrateKeypairFactory
import io.novasama.substrate_sdk_android.ss58.SS58Encoder.toAddress
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

private const val PASSWORD = "12345"
private const val NAME = "test"

@RunWith(MockitoJUnitRunner::class)
class JsonSeedEncoderTest {
    private val gson = Gson()

    private val decoder = JsonSeedDecoder(gson)
    private val encoder = JsonSeedEncoder(gson)

    @Test
    fun `should encode ed25519`() {
        performTest(MultiChainEncryption.Substrate(EncryptionType.ED25519))
    }

    @Test
    fun `should encode ecdsa`() {
        performTest(MultiChainEncryption.Substrate(EncryptionType.ECDSA))
    }

    @Test
    fun `should encode ethereum`() {
        performTest(MultiChainEncryption.Ethereum)
    }

    @Test
    @Ignore("sr25519 is not supported in unit tests")
    fun `should encode sr25519`() {
        performTest(MultiChainEncryption.Substrate(EncryptionType.SR25519))
    }

    private fun performTest(multiChainEncryption: MultiChainEncryption) {
        val seedExpected = TestData.SEED_BYTES
        val keypairExpected = SubstrateKeypairFactory.generate(multiChainEncryption.encryptionType, seedExpected)

        val address = keypairExpected.publicKey.toAddress(TestAddressBytes.WESTEND)

        val json = encoder.generate(
            keypair = keypairExpected,
            seed = seedExpected,
            password = PASSWORD,
            name = NAME,
            multiChainEncryption = multiChainEncryption,
            address = address,
            genesisHash = TestGeneses.WESTEND
        )

        val decoded = decoder.decode(json, PASSWORD)

        with(decoded) {
            assert(keypairExpected.publicKey.contentEquals(keypair.publicKey))
            assert(keypairExpected.privateKey.contentEquals(keypair.privateKey))
            assertEquals(NAME, username)

            seed?.let {
                assertArrayEquals(seedExpected, it)
            }
        }
    }
}