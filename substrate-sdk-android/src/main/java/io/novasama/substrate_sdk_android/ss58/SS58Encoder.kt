package io.novasama.substrate_sdk_android.ss58

import io.novasama.substrate_sdk_android.encrypt.Base58
import io.novasama.substrate_sdk_android.encrypt.json.copyBytes
import io.novasama.substrate_sdk_android.exceptions.AddressFormatException
import io.novasama.substrate_sdk_android.extensions.shl
import io.novasama.substrate_sdk_android.extensions.shr
import io.novasama.substrate_sdk_android.hash.Hasher.blake2b256
import io.novasama.substrate_sdk_android.hash.Hasher.blake2b512
import java.lang.Exception
import kotlin.experimental.or

object SS58Encoder {

    private val PREFIX = "SS58PRE".toByteArray(Charsets.UTF_8)
    private const val PREFIX_SIZE = 2
    private const val PUBLIC_KEY_SIZE = 32

    private val base58 = io.novasama.substrate_sdk_android.encrypt.Base58()

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun getPrefixLenIdent(decodedByteArray: ByteArray): Pair<Int, Short> {
        return when {
            decodedByteArray[0] in 0..63 -> 1 to decodedByteArray[0].toShort()
            decodedByteArray[0] in 64..127 -> {
                val lower = (decodedByteArray[0].toUByte() shl 2) or (decodedByteArray[1].toUByte() shr 6)
                val upper = (decodedByteArray[1].toUByte() and 0b00111111.toUByte())

                2 to (lower.toInt() or (upper.toInt() shl 8)).toShort()
            }
            else -> throw IllegalArgumentException("Incorrect address byte")
        }
    }

    fun encode(publicKey: ByteArray, addressPrefix: Short): String {
        val normalizedKey = publicKey.publicKeyToSubstrateAccountId()
        val ident = addressPrefix.toInt() and 0b0011_1111_1111_1111
        val addressTypeByteArray = when (ident) {
            in 0..63 -> byteArrayOf(ident.toByte())
            in 64..16_383 -> {
                val first = (ident and 0b0000_0000_1111_1100) shr 2
                val second = (ident shr 8) or ((ident and 0b0000_0000_0000_0011) shl 6)
                byteArrayOf(first.toByte() or 0b01000000, second.toByte())
            }
            else -> throw IllegalArgumentException("Reserved for future address format extensions")
        }

        val hash = (PREFIX + addressTypeByteArray + normalizedKey).blake2b512()
        val checksum = hash.copyOfRange(0, PREFIX_SIZE)

        val resultByteArray = addressTypeByteArray + normalizedKey + checksum

        return base58.encode(resultByteArray)
    }

    @Throws(IllegalArgumentException::class)
    fun decode(ss58String: String): ByteArray {
        val decodedByteArray = base58.decode(ss58String)
        if (decodedByteArray.size < 2) throw IllegalArgumentException("Invalid address")
        val (prefixLen, _) = getPrefixLenIdent(decodedByteArray)
        val hash = (PREFIX + decodedByteArray.copyBytes(0, PUBLIC_KEY_SIZE + prefixLen)).blake2b512()
        val checkSum = hash.copyBytes(0, PREFIX_SIZE)
        if (!checkSum.contentEquals(decodedByteArray.copyBytes(PUBLIC_KEY_SIZE + prefixLen, PREFIX_SIZE))) {
            throw IllegalArgumentException("Invalid checksum")
        }
        return decodedByteArray.copyBytes(prefixLen, PUBLIC_KEY_SIZE)
    }

    @Throws(AddressFormatException::class)
    fun extractAddressPrefix(address: String): Short {
        val decodedByteArray = base58.decode(address)
        if (decodedByteArray.size < 2) throw IllegalArgumentException("Invalid address")
        val (_, ident) = getPrefixLenIdent(decodedByteArray)
        return ident
    }

    fun extractAddressByteOrNull(address: String): Short? = try {
        extractAddressPrefix(address)
    } catch (e: Exception) {
        null
    }

    fun ByteArray.publicKeyToSubstrateAccountId() = if (size > 32) {
        blake2b256()
    } else {
        this
    }

    fun ByteArray.toAddress(addressPrefix: Short) = encode(this, addressPrefix)

    fun String.toAccountId() = decode(this)

    fun String.addressPrefix() = extractAddressPrefix(this)

    fun String.addressByteOrNull() = extractAddressByteOrNull(this)
}
