package io.novasama.substrate_sdk_android.encrypt.keypair.substrate

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import io.novasama.substrate_sdk_android.encrypt.junction.Junction
import io.novasama.substrate_sdk_android.encrypt.junction.SubstrateJunctionDecoder
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.encrypt.keypair.deriveKeyPair
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

    fun decodeSr25519Keypair(
        encryptedKey: ByteArray,
        junctions: List<Junction> = emptyList()
    ): Keypair = Sr25519SubstrateKeypairFactory.deriveKeyPair(
        Sr25519SubstrateKeypairFactory.deriveEncryptedKeypair(encryptedKey),
        junctions
    )

    fun generate(
        encryptionType: EncryptionType,
        seed: ByteArray,
        derivationPath: String?
    ): Keypair {
        val junctions = getJunctions(derivationPath)
        return generate(encryptionType, seed, junctions)
    }

    fun decodeSr25519Keypair(
        rawKey: ByteArray,
        derivationPath: String?
    ): Keypair {
        val junctions = getJunctions(derivationPath)
        return decodeSr25519Keypair(rawKey, junctions)
    }

    private fun getJunctions(derivationPath: String?): List<Junction> {
        return if (derivationPath != null) {
            SubstrateJunctionDecoder.decode(derivationPath).junctions
        } else {
            emptyList()
        }
    }
}
