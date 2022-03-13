package jp.co.soramitsu.fearless_utils.keyring.seed.ethereum

import jp.co.soramitsu.fearless_utils.keyring.mnemonic.Mnemonic
import jp.co.soramitsu.fearless_utils.keyring.mnemonic.MnemonicCreator
import jp.co.soramitsu.fearless_utils.keyring.seed.SeedCreator
import jp.co.soramitsu.fearless_utils.keyring.seed.SeedFactory

object EthereumSeedFactory : SeedFactory {

    override fun createSeed(length: Mnemonic.Length, password: String?): SeedFactory.Result {
        val mnemonic = MnemonicCreator.randomMnemonic(length)
        val seed = SeedCreator.deriveSeed(mnemonic.words.encodeToByteArray(), password)

        return SeedFactory.Result(seed, mnemonic)
    }

    override fun deriveSeed(mnemonicWords: String, password: String?): SeedFactory.Result {
        val mnemonic = MnemonicCreator.fromWords(mnemonicWords)
        val seed = SeedCreator.deriveSeed(mnemonic.words.encodeToByteArray(), password)

        return SeedFactory.Result(seed, mnemonic)
    }
}
