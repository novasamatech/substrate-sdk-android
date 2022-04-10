package jp.co.soramitsu.fearless_utils.keyring.signing.extrinsic

import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import java.util.Locale

val EncryptionType.multiSignatureName
    get() = rawName.capitalize(Locale.ROOT)
