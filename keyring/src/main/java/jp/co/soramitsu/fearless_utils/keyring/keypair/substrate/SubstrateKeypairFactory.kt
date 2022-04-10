package jp.co.soramitsu.fearless_utils.keyring.keypair.substrate

import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import jp.co.soramitsu.fearless_utils.keyring.junction.Junction
import jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair
import jp.co.soramitsu.fearless_utils.keyring.keypair.generate

object SubstrateKeypairFactory {

    fun generate(
        encryptionType: EncryptionType,
        seed: ByteArray,
        junctions: List<Junction> = emptyList()
    ): Keypair = when (encryptionType) {
        EncryptionType.SR25519 -> Sr25519SubstrateKeypairFactory.generate(seed, junctions)
        EncryptionType.ED25519 -> Ed25519SubstrateKeypairFactory.generate(seed, junctions)
        EncryptionType.ECDSA -> ECDSASubstrateKeypairFactory.generate(seed, junctions)
    }
}
