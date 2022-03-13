package jp.co.soramitsu.fearless_utils.keyring.signing.extrinsic.signer

import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import jp.co.soramitsu.fearless_utils.keyring.keypair.BaseKeypair
import jp.co.soramitsu.fearless_utils.keyring.keypair.ECDSAUtils
import jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair
import jp.co.soramitsu.fearless_utils.keyring.keypair.derivePublicKey
import jp.co.soramitsu.fearless_utils.signing.Signer

class FeeSigner(val feeKeypair: Keypair): KeypairSigner(feeKeypair)

fun FeeSigner(): FeeSigner {
    val privateKey = ByteArray(32) { 1 }

    val keypair = BaseKeypair(
        privateKey = privateKey,
        publicKey = ECDSAUtils.derivePublicKey(
            privateKey
        ),
        encryptionType = EncryptionType.ECDSA
    )

    return FeeSigner(keypair)
}
