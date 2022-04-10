package jp.co.soramitsu.fearless_utils.keyring.keypair.substrate

import jp.co.soramitsu.fearless_utils.address.PublicKey
import jp.co.soramitsu.fearless_utils.address.asPublicKey
import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import jp.co.soramitsu.fearless_utils.keyring.Sr25519
import jp.co.soramitsu.fearless_utils.keyring.junction.Junction
import jp.co.soramitsu.fearless_utils.keyring.junction.JunctionType
import jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair
import jp.co.soramitsu.fearless_utils.keyring.keypair.KeypairFactory

class Sr25519Keypair(
    override val privateKey: ByteArray,
    override val publicKey: PublicKey,
    val nonce: ByteArray
) : Keypair {

    override val encryptionType: EncryptionType = EncryptionType.SR25519
}

internal object Sr25519SubstrateKeypairFactory : KeypairFactory<Sr25519Keypair> {
    override fun deriveFromSeed(seed: ByteArray): Sr25519Keypair {
        val keypairBytes = Sr25519.keypairFromSeed(seed)

        return decodeSr25519Keypair(keypairBytes)
    }

    override fun deriveChild(parent: Sr25519Keypair, junction: Junction): Sr25519Keypair {
        return when (junction.type) {
            JunctionType.SOFT -> deriveSr25519SoftKeypair(junction.chaincode, parent)
            JunctionType.HARD -> deriveSr25519HardKeypair(junction.chaincode, parent)
        }
    }

    private fun deriveSr25519SoftKeypair(
        chaincode: ByteArray,
        previousKeypair: Sr25519Keypair
    ): Sr25519Keypair {
        val keypair = previousKeypair.privateKey + previousKeypair.nonce + previousKeypair.publicKey.value
        val newKeypairbytes = Sr25519.deriveKeypairSoft(keypair, chaincode)

        return decodeSr25519Keypair(newKeypairbytes)
    }

    private fun deriveSr25519HardKeypair(
        chaincode: ByteArray,
        previousKeypair: Sr25519Keypair
    ): Sr25519Keypair {
        val keypair = previousKeypair.privateKey + previousKeypair.nonce + previousKeypair.publicKey.value
        val newKeypairbytes = Sr25519.deriveKeypairHard(keypair, chaincode)

        return decodeSr25519Keypair(newKeypairbytes)
    }

    private fun decodeSr25519Keypair(bytes: ByteArray): Sr25519Keypair {
        val privateKey = bytes.copyOfRange(0, 32)
        val nonce = bytes.copyOfRange(32, 64)
        val publicKey = bytes.copyOfRange(64, bytes.size)
        return Sr25519Keypair(
            privateKey,
            publicKey.asPublicKey(),
            nonce
        )
    }
}
