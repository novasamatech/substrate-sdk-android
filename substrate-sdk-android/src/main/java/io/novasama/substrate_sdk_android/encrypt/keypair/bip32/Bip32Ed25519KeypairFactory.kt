package io.novasama.substrate_sdk_android.encrypt.keypair.bip32

import io.novasama.substrate_sdk_android.encrypt.SecurityProviders
import io.novasama.substrate_sdk_android.encrypt.hmacSHA512
import io.novasama.substrate_sdk_android.encrypt.junction.Junction
import io.novasama.substrate_sdk_android.encrypt.junction.JunctionType
import io.novasama.substrate_sdk_android.encrypt.keypair.Ed25519Utils
import io.novasama.substrate_sdk_android.extensions.fromUnsignedBytes
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import java.math.BigInteger

object Bip32Ed25519KeypairFactory : Bip32KeypairFactory() {

    private val CURVE_SEED = "ed25519 seed".encodeToByteArray()

    private const val PRIVATE_KEY_SIZE = 32

    private const val MAX_CHAINCODE_SIZE = 4

    init {
        SecurityProviders.requireBouncyCastle
    }

    override fun deriveFromSeed(seed: ByteArray): Bip32ExtendedKeyPair {
        val hmacResult = seed.hmacSHA512(secret = CURVE_SEED)

        val privateKey = hmacResult.sliceArray(0 until PRIVATE_KEY_SIZE)
        val chainCode = hmacResult.sliceArray(PRIVATE_KEY_SIZE until hmacResult.size)

        return Bip32ExtendedKeyPair(
            privateKey = privateKey,
            publicKey = Ed25519Utils.derivePublicFromPrivate(privateKey),
            chaincode = chainCode
        )
    }

    override fun deriveChild(
        parent: Bip32ExtendedKeyPair,
        junction: Junction
    ): Bip32ExtendedKeyPair {
        require(junction.type == JunctionType.HARD) { "Ed25519 only supports hardened derivation" }
        require(junction.chaincode.size <= MAX_CHAINCODE_SIZE) {
            "Invalid chaincode: size is ${junction.chaincode.size}, max allowed: $MAX_CHAINCODE_SIZE"
        }

        require(junction.chaincode.fromUnsignedBytes() >= BigInteger.ZERO) {
            "Child index must be non-negative"
        }

        return deriveChildKey(parent, junction)
    }

    private fun deriveChildKey(
        parent: Bip32ExtendedKeyPair,
        junction: Junction
    ): Bip32ExtendedKeyPair {
        val padding = byteArrayOf(0)
        val sourceData = padding + parent.privateKey + junction.chaincode

        val output = sourceData.hmacSHA512(secret = parent.chaincode)

        val privateKey = output.sliceArray(0 until PRIVATE_KEY_SIZE)
        val childChainCode = output.sliceArray(PRIVATE_KEY_SIZE until output.size)

        return Bip32ExtendedKeyPair(
            privateKey = privateKey,
            publicKey = Ed25519Utils.derivePublicFromPrivate(privateKey),
            chaincode = childChainCode
        )
    }
}
