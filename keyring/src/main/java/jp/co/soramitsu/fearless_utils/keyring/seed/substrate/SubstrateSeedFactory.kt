package jp.co.soramitsu.fearless_utils.keyring.seed.substrate

import jp.co.soramitsu.fearless_utils.keyring.mnemonic.Mnemonic
import jp.co.soramitsu.fearless_utils.keyring.mnemonic.MnemonicCreator
import jp.co.soramitsu.fearless_utils.keyring.seed.SeedCreator
import jp.co.soramitsu.fearless_utils.keyring.seed.SeedFactory

object SubstrateSeedFactory : SeedFactory {

    override fun createSeed(length: Mnemonic.Length, password: String?): SeedFactory.Result {
        val mnemonic = MnemonicCreator.randomMnemonic(length)
        val seed = SeedCreator.deriveSeed(mnemonic.entropy, password)

        return SeedFactory.Result(seed, mnemonic)
    }

    override fun deriveSeed(mnemonicWords: String, password: String?): SeedFactory.Result {
        val mnemonic = MnemonicCreator.fromWords(mnemonicWords)
        val seed = SeedCreator.deriveSeed(mnemonic.entropy, password)

        return SeedFactory.Result(seed, mnemonic)
    }
}
