package jp.co.soramitsu.fearless_utils.samples.decoratable_api

import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import jp.co.soramitsu.fearless_utils.keyring.Keyring
import jp.co.soramitsu.fearless_utils.keyring.MultiChainEncryption

fun jp.co.soramitsu.fearless_utils.keyring.Keyring.sampleAccount() = fromMnemonic(
    mnemonicPhrase = "awful issue penalty frog jungle black frost reward disease whale snap attract",
    multiChainEncryption = jp.co.soramitsu.fearless_utils.keyring.MultiChainEncryption.Substrate(jp.co.soramitsu.fearless_utils.keyring.EncryptionType.ED25519)
)
