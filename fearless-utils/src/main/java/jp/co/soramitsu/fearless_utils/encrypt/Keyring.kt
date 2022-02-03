package jp.co.soramitsu.fearless_utils.encrypt

import jp.co.soramitsu.fearless_utils.encrypt.junction.BIP32JunctionDecoder
import jp.co.soramitsu.fearless_utils.encrypt.junction.SubstrateJunctionDecoder
import jp.co.soramitsu.fearless_utils.encrypt.keypair.Keypair
import jp.co.soramitsu.fearless_utils.encrypt.keypair.ethereum.EthereumKeypairFactory
import jp.co.soramitsu.fearless_utils.encrypt.keypair.substrate.SubstrateKeypairFactory
import jp.co.soramitsu.fearless_utils.encrypt.seed.deriveSeed32
import jp.co.soramitsu.fearless_utils.encrypt.seed.ethereum.EthereumSeedFactory
import jp.co.soramitsu.fearless_utils.encrypt.seed.substrate.SubstrateSeedFactory

object Keyring {

    /**
     * @param mnemonicPhrase - string of words separated by space
     * @return keypair generated using supplied encryption algorithm
     */
    fun fromMnemonic(
        mnemonicPhrase: String,
        multiChainEncryption: MultiChainEncryption,
        derivationPath: String? = null,
    ): Result<Keypair> {
        return runCatching {
            when (multiChainEncryption) {
                is MultiChainEncryption.Ethereum -> {
                    val decodedDerivationPath = derivationPath?.let(BIP32JunctionDecoder::decode)
                    val seed = EthereumSeedFactory.deriveSeed(mnemonicPhrase, password = decodedDerivationPath?.password).seed

                    EthereumKeypairFactory.generate(seed, decodedDerivationPath?.junctions.orEmpty())
                }
                is MultiChainEncryption.Substrate -> {
                    val decodedDerivationPath = derivationPath?.let(SubstrateJunctionDecoder::decode)
                    val seed = SubstrateSeedFactory.deriveSeed32(mnemonicPhrase, password = decodedDerivationPath?.password).seed

                    SubstrateKeypairFactory.generate(multiChainEncryption.encryptionType, seed, decodedDerivationPath?.junctions.orEmpty())
                }
            }
        }
    }
}
