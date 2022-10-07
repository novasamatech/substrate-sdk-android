package jp.co.soramitsu.fearless_utils.extensions

import jp.co.soramitsu.fearless_utils.hash.isNegative
import jp.co.soramitsu.fearless_utils.hash.isPositive
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder

inline fun <T, R> Iterable<T>.tryFindNonNull(transform: (T) -> R?): R? {
    for (item in this) {
        val transformed = transform(item)

        if (transformed != null) return transformed
    }

    return null
}

private const val UNSIGNED_SIGNUM = 1

fun ByteArray.fromUnsignedBytes(originByteOrder: ByteOrder = ByteOrder.BIG_ENDIAN): BigInteger {
    val ordered = toBigIntegerByteOrder(originByteOrder)

    return BigInteger(UNSIGNED_SIGNUM, ordered)
}

fun ByteArray.fromSignedBytes(originByteOrder: ByteOrder = ByteOrder.BIG_ENDIAN): BigInteger {
    val ordered = toBigIntegerByteOrder(originByteOrder)

    return BigInteger(ordered)
}

fun BigInteger.toSignedBytes(
    resultByteOrder: ByteOrder = ByteOrder.BIG_ENDIAN,
    expectedBytesSize: Int
): ByteArray {
    val signedBytes = toByteArray()
    // 0xff if negative else 0x00 as per two-complement notation
    val padding: Byte = if (isNegative()) -1 else 0

    val padded = signedBytes.pad(expectedBytesSize, padding)

    return padded.fromBigIntegerByteOrder(resultByteOrder = resultByteOrder)
}

fun ByteArray.pad(expectedSize: Int, padding: Byte = 0): ByteArray {
    if (size >= expectedSize) return this

    val padded = ByteArray(expectedSize) { padding }
    val startAt = expectedSize - size
    return copyInto(padded, startAt)
}

private fun ByteArray.fromBigIntegerByteOrder(resultByteOrder: ByteOrder): ByteArray {
    // Big Integer uses big endian numbers
    return if (resultByteOrder == ByteOrder.LITTLE_ENDIAN) reversedArray() else this
}

private fun ByteArray.toBigIntegerByteOrder(originByteOrder: ByteOrder): ByteArray {
    // Big Integer accepts big endian numbers
    return if (originByteOrder == ByteOrder.LITTLE_ENDIAN) reversedArray() else this
}

@ExperimentalUnsignedTypes
fun UInt.toUnsignedBytes(order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray {
    return ByteBuffer.allocate(Int.SIZE_BYTES).also {
        it.order(order)
        it.putInt(this.toInt())
    }.array()
}

fun ByteArray.split(divider: ByteArray): List<ByteArray> {

    var elementStart = 0

    val dividerSize = divider.size
    var dividerIndex = 0

    val results = mutableListOf<ByteArray>()

    forEachIndexed { index, byte ->
        if (byte == divider[dividerIndex]) {
            dividerIndex += 1

            if (dividerIndex == dividerSize) {
                val elementEnd = index - dividerSize + 1

                if (elementStart < elementEnd) {
                    results.add(copyOfRange(elementStart, elementEnd))
                } else {
                    results.add(byteArrayOf())
                }

                dividerIndex = 0
                elementStart = index + 1
            }
        } else {
            dividerIndex = 0
        }
    }

    if (elementStart < size) {
        results.add(copyOfRange(elementStart, size))
    }

    return results
}

internal fun String.snakeCaseToCamelCase(): String {
    return split("_").mapIndexed { index, segment ->
        if (index > 0) { // do not capitalize first segment
            segment.capitalize()
        } else {
            segment
        }
    }.joinToString(separator = "")
}

@ExperimentalUnsignedTypes
/**
 * Unsafe to overflow
 */
infix fun UByte.shl(numOfBytes: Int) = (toInt() shl numOfBytes).toUByte()

@ExperimentalUnsignedTypes
/**
 * Unsafe to overflow
 */
infix fun UByte.shr(numOfBytes: Int) = (toInt() shr numOfBytes).toUByte()

fun ByteArray.copyLast(n: Int) = copyOfRange(fromIndex = size - n, size)
