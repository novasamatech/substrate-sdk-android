package jp.co.soramitsu.fearless_utils.keyring.signing.extrinsic

import jp.co.soramitsu.fearless_utils.keyring.EncryptionType

val EncryptionType.multiSignatureName
    get() = rawName.capitalize()
