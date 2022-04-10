package jp.co.soramitsu.fearless_utils.keyring.keypair.substrate

import jp.co.soramitsu.fearless_utils.address.asPublicKey
import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import jp.co.soramitsu.fearless_utils.keyring.keypair.ECDSAUtils
import jp.co.soramitsu.fearless_utils.keyring.keypair.derivePublicKey

internal object ECDSASubstrateKeypairFactory : OtherSubstrateKeypairFactory("Secp256k1HDKD") {

    override fun deriveFromSeed(seed: ByteArray): KeypairWithSeed {
        return KeypairWithSeed(
            seed = seed,
            privateKey = seed,
            publicKey = ECDSAUtils.derivePublicKey(privateKeyOrSeed = seed).asPublicKey(),
            encryptionType = EncryptionType.ECDSA
        )
    }
}
