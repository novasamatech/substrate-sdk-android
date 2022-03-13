package jp.co.soramitsu.fearless_utils.decoratable_api.tx.fee

import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import jp.co.soramitsu.fearless_utils.keyring.Keyring
import jp.co.soramitsu.fearless_utils.keyring.keypair.BaseKeypair
import jp.co.soramitsu.fearless_utils.keyring.keypair.ECDSAUtils
import jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair
import jp.co.soramitsu.fearless_utils.keyring.keypair.derivePublicKey

// Sign fee extrinsic with ECDSA keypair - it provides the most compatibility
internal fun jp.co.soramitsu.fearless_utils.keyring.Keyring.feeSigner(): jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair {
    val privateKey = ByteArray(32) { 1 }

    return jp.co.soramitsu.fearless_utils.keyring.keypair.BaseKeypair(
        privateKey = privateKey,
        publicKey = jp.co.soramitsu.fearless_utils.keyring.keypair.ECDSAUtils.derivePublicKey(
            privateKey
        ),
        encryptionType = jp.co.soramitsu.fearless_utils.keyring.EncryptionType.ECDSA
    )
}
