package jp.co.soramitsu.fearless_utils.decoratable_api.tx.fee

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.Keyring
import jp.co.soramitsu.fearless_utils.encrypt.keypair.BaseKeypair
import jp.co.soramitsu.fearless_utils.encrypt.keypair.ECDSAUtils
import jp.co.soramitsu.fearless_utils.encrypt.keypair.Keypair
import jp.co.soramitsu.fearless_utils.encrypt.keypair.derivePublicKey

// Sign fee extrinsic with ECDSA keypair - it provides the most compatibility
internal fun Keyring.feeSigner(): Keypair {
    val privateKey = ByteArray(32) { 1 }

    return BaseKeypair(
        privateKey = privateKey,
        publicKey = ECDSAUtils.derivePublicKey(privateKey),
        encryptionType = EncryptionType.ECDSA
    )
}
