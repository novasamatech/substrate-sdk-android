package io.novasama.substrate_sdk_android.encrypt.keypair.substrate

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import io.novasama.substrate_sdk_android.encrypt.junction.Junction
import io.novasama.substrate_sdk_android.encrypt.junction.SubstrateJunctionDecoder
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.encrypt.keypair.generate
import io.novasama.substrate_sdk_android.encrypt.keypair.generateFromRaw

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

    fun generateFromRaw(
        encryptionType: EncryptionType,
        rawKey: ByteArray,
        junctions: List<Junction> = emptyList()
    ): Keypair = when (encryptionType) {
        EncryptionType.SR25519 -> Sr25519SubstrateKeypairFactory.generateFromRaw(rawKey, junctions)
        EncryptionType.ED25519 -> Ed25519SubstrateKeypairFactory.generateFromRaw(rawKey, junctions)
        EncryptionType.ECDSA -> ECDSASubstrateKeypairFactory.generateFromRaw(rawKey, junctions)
    }

    fun generate(
        encryptionType: EncryptionType,
        seed: ByteArray,
        derivationPath: String?
    ): Keypair {
        val junctions = getJunctions(derivationPath)
        return generate(encryptionType, seed, junctions)
    }

    fun generateFromRaw(
        encryptionType: EncryptionType,
        rawKey: ByteArray,
        derivationPath: String?
    ): Keypair {
        val junctions = getJunctions(derivationPath)
        return generateFromRaw(encryptionType, rawKey, junctions)
    }

    private fun getJunctions(derivationPath: String?): List<Junction> {
        return if (derivationPath != null) {
            SubstrateJunctionDecoder.decode(derivationPath).junctions
        } else {
            emptyList()
        }
    }
}
