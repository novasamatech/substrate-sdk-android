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

abstract class BaseSubstrateJsonEncoderTest : BaseJsonEncoderTest() {

    abstract val encryptionType: EncryptionType

    override fun deriveSeedAndKeypair(mnemonic: String, derivationPath: String?): DerivationResult {
        val derivationPathDecoded = SubstrateJunctionDecoder.decode(derivationPath)
        val seed = SubstrateSeedFactory.deriveSeed32(mnemonic, password = derivationPathDecoded.password())
        val keypair = SubstrateKeypairFactory.generate(encryptionType, seed.seed, derivationPathDecoded.junctions())

        return DerivationResult(
            seed = seed.seed,
            keypair = keypair,
            encryption = MultiChainEncryption.Substrate(encryptionType)
        )
    }
}