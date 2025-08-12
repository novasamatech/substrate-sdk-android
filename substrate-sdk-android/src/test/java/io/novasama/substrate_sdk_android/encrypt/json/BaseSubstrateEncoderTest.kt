package io.novasama.substrate_sdk_android.encrypt.json

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.junction.SubstrateJunctionDecoder
import io.novasama.substrate_sdk_android.encrypt.junction.decode
import io.novasama.substrate_sdk_android.encrypt.junction.junctions
import io.novasama.substrate_sdk_android.encrypt.junction.password
import io.novasama.substrate_sdk_android.encrypt.keypair.substrate.SubstrateKeypairFactory
import io.novasama.substrate_sdk_android.encrypt.seed.substrate.SubstrateSeedFactory
import io.novasama.substrate_sdk_android.encrypt.seed.substrate.deriveSeed32
import io.novasama.substrate_sdk_android.ss58.SS58Encoder.toAddress

abstract class BaseSubstrateJsonEncoderTest : BaseJsonEncoderTest() {

    abstract val encryptionType: EncryptionType

    protected fun testExternalCompatibility(derivationPath: String?, json: String) {
        val derivationData = createDerivationData(derivationPath)
        val testCase = ExternalCompatibilityTestCase(derivationData, json, password)
        testExternalCompatibility(testCase)
    }

    protected fun testSelfCompatibility(derivationPath: String?) {
        val testCase = SelfCompatibilityTestCase(createDerivationData(derivationPath))
        testSelfCompatibility(testCase)
    }

    private fun createDerivationData(derivationPath: String?): DerivationData {
        val derivationPathDecoded = SubstrateJunctionDecoder.decode(derivationPath)
        val seed = SubstrateSeedFactory.deriveSeed32(testMnemonic, password = derivationPathDecoded.password())
        val keypair = SubstrateKeypairFactory.generate(encryptionType, seed.seed, derivationPathDecoded.junctions())

        return DerivationData(
            keypair = keypair,
            encryption = MultiChainEncryption.Substrate(encryptionType),
            address = keypair.publicKey.toAddress(addressPrefix = 0)
        )
    }
}