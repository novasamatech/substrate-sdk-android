package jp.co.soramitsu.fearless_utils.keyring.keypair.substrate

import jp.co.soramitsu.fearless_utils.address.PublicKey
import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import jp.co.soramitsu.fearless_utils.keyring.junction.Junction
import jp.co.soramitsu.fearless_utils.keyring.junction.JunctionType
import jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair
import jp.co.soramitsu.fearless_utils.keyring.keypair.KeypairFactory
import jp.co.soramitsu.fearless_utils.hash.Hasher.blake2b256
import jp.co.soramitsu.fearless_utils.scale.dataType.string
import jp.co.soramitsu.fearless_utils.scale.dataType.toByteArray

class KeypairWithSeed(
    val seed: ByteArray,
    override val privateKey: ByteArray,
    override val publicKey: PublicKey,
    override val encryptionType: EncryptionType
) : Keypair

abstract class OtherSubstrateKeypairFactory(
    private val hardDerivationPrefix: String
) : KeypairFactory<KeypairWithSeed> {

    override fun deriveChild(parent: KeypairWithSeed, junction: Junction): KeypairWithSeed {
        if (junction.type == JunctionType.HARD) {
            val prefix = string.toByteArray(hardDerivationPrefix)

            val newSeed = (prefix + parent.seed + junction.chaincode).blake2b256()

            return deriveFromSeed(newSeed)
        } else {
            throw KeypairFactory.SoftDerivationNotSupported()
        }
    }
}
