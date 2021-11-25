package jp.co.soramitsu.fearless_utils.samples.decoratable_api

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.Keyring
import jp.co.soramitsu.fearless_utils.encrypt.MultiChainEncryption


fun Keyring.sampleAccount() = fromMnemonic(
    mnemonicPhrase = "awful issue penalty frog jungle black frost reward disease whale snap attract",
    multiChainEncryption = MultiChainEncryption.Substrate(EncryptionType.ED25519)
)
