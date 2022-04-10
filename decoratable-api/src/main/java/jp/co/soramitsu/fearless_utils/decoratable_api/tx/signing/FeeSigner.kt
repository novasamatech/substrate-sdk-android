package jp.co.soramitsu.fearless_utils.decoratable_api.tx.signing

import jp.co.soramitsu.fearless_utils.address.asPublicKey
import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import jp.co.soramitsu.fearless_utils.keyring.keypair.BaseKeypair
import jp.co.soramitsu.fearless_utils.keyring.keypair.ECDSAUtils
import jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair
import jp.co.soramitsu.fearless_utils.keyring.keypair.derivePublicKey

class FeeSigner(val feeKeypair: Keypair, api: SubstrateApi) : KeypairSigner(feeKeypair, api)

fun FeeSigner(api: SubstrateApi): FeeSigner {
    val privateKey = ByteArray(32) { 1 }

    val keypair = BaseKeypair(
        privateKey = privateKey,
        publicKey = ECDSAUtils.derivePublicKey(
            privateKey
        ).asPublicKey(),
        encryptionType = EncryptionType.ECDSA
    )

    return FeeSigner(keypair, api)
}
