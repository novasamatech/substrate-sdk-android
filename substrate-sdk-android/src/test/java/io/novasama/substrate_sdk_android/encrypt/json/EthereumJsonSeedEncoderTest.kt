package io.novasama.substrate_sdk_android.encrypt.json

import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.junction.BIP32JunctionDecoder
import io.novasama.substrate_sdk_android.encrypt.junction.junctions
import io.novasama.substrate_sdk_android.encrypt.junction.password
import io.novasama.substrate_sdk_android.encrypt.keypair.bip32.Bip32EcdsaKeypairFactory
import io.novasama.substrate_sdk_android.encrypt.keypair.generate
import io.novasama.substrate_sdk_android.encrypt.seed.bip39.Bip39SeedFactory
import io.novasama.substrate_sdk_android.extensions.asEthereumPublicKey
import io.novasama.substrate_sdk_android.extensions.toAddress
import org.junit.Test

class EthereumJsonSeedEncoderTest : BaseJsonEncoderTest() {

    @Test
    fun `encode should be compatible with decode with derivation path`() {
        testSelfCompatibility(derivationPath = "//44//60//0/0/0")
    }

    @Test
    fun `decode should be compatible with external sources with derivation path`() {
        testExternalCompatibility(
            derivationPath = "//44//60//0/0/0",
            json = """{"encoded":"32UIVT9e6N3iDU4m5MJTZmasqVqJJhCvs2zEe0+aKzoAAAIAAQAAAAgAAAAgrLrM0SWabwoeaOVQNFJilMsQVcE38OmsplYzRVonvkGScxRDClp9T3KTxOJaeLDIB1SVgfinoXjuqrQ1YWUrJKNyg2dwmbadiBtK7dD5v9FJGBiwpKEQjZyDJHNWV5PSRMe4b0x+U3Af56zFOevOjT1/oT6AeLX2qV8fPGU=","encoding":{"content":["pkcs8","ethereum"],"type":["scrypt","xsalsa20-poly1305"],"version":"3"},"address":"0x02bcc434df9aa5ea2fc72717dbcbbd4beb2588cc2e86b4fcd704b03efb468ca1d0","meta":{"genesisHash":"0xfe58ea77779b7abda7da4ec526d14db9b1e9cd40a217c34892af80a9b332b76d","isHardware":false,"name":"test","tags":[],"whenCreated":1754995559657}}"""
        )
    }

    private fun testExternalCompatibility(derivationPath: String, json: String) {
        val derivationData = createDerivationData(derivationPath)
        val testCase = ExternalCompatibilityTestCase(derivationData, json, password)
        testExternalCompatibility(testCase)
    }

    private fun testSelfCompatibility(derivationPath: String) {
        val testCase = SelfCompatibilityTestCase(createDerivationData(derivationPath))
        testSelfCompatibility(testCase)
    }

    private fun createDerivationData(derivationPath: String): DerivationData {
        val derivationPathDecoded = BIP32JunctionDecoder.decode(derivationPath)
        val seed = Bip39SeedFactory.deriveSeed(testMnemonic, password = derivationPathDecoded.password())
        val keypair = Bip32EcdsaKeypairFactory.generate(seed.seed, derivationPathDecoded.junctions())

        return DerivationData(
            seed = seed.seed,
            keypair = keypair,
            encryption = MultiChainEncryption.Ethereum,
            address = keypair.publicKey.asEthereumPublicKey().toAddress().value
        )
    }
}