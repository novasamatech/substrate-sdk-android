package io.novasama.substrate_sdk_android.encrypt.keypair.substrate

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import io.novasama.substrate_sdk_android.encrypt.junction.Junction
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.encrypt.keypair.generate

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
