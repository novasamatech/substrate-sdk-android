package io.novasama.substrate_sdk_android.encrypt.keypair

import io.novasama.substrate_sdk_android.extensions.toHexString
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.util.encoders.Hex
import org.web3j.crypto.Sign
import java.math.BigInteger

object ECDSAUtils {

    fun compressedPublicKeyFromPrivate(privKey: BigInteger): ByteArray {
        val point = Sign.publicPointFromPrivate(privKey)
        return point.getEncoded(true)
    }

    fun decompressedAsInt(compressedKey: ByteArray): BigInteger {
        val decompressedArray = decompressed(compressedKey)

        return Hex.toHexString(byteArrayOf(0x00) + decompressedArray).toBigInteger(16)
    }

    fun decompressed(compressedKey: ByteArray): ByteArray {
        val spec = ECNamedCurveTable.getParameterSpec("secp256k1")
        val point = spec.curve.decodePoint(compressedKey)
        val x: ByteArray = point.xCoord.encoded
        val y: ByteArray = point.yCoord.encoded

        return x + y
    }
}

fun ECDSAUtils.derivePublicKey(privateKeyOrSeed: ByteArray): ByteArray {
    val privateKeyInt = BigInteger(privateKeyOrSeed.toHexString(), 16)

    return compressedPublicKeyFromPrivate(privateKeyInt)
}
