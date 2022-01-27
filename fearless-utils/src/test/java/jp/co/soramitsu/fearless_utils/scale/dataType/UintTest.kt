package jp.co.soramitsu.fearless_utils.scale.dataType

import org.junit.Assert.assertArrayEquals
import org.junit.Test

@OptIn(ExperimentalUnsignedTypes::class)
class UintTest {

    private val u64Tests = listOf(
        "578437695752307201" to byteArrayOf(1U, 2U, 3U, 4U, 5U, 6U, 7U, 8U),
    )

    private val u128Tests = listOf(
        // when BigInteger inserts extra byte for sign
        "182365888117048807484804376330534607370" to byteArrayOf(10, 90, -81, 66, 0, 30, -30, -32, -31, 87, -99, 77, 121, 100, 50, -119)
    )

    @Test
    fun `u64 tests`() = runTests(uint64, u64Tests)

    @Test
    fun `u128 tests`() = runTests(uint128, u128Tests)

    private fun runTests(type: uint, tests: List<Pair<String, ByteArray>>)  {
        tests.forEach { (numberRaw, expected) ->
            val number = numberRaw.toBigInteger()

            assertArrayEquals(expected, type.toByteArray(number))
        }
    }

    private fun byteArrayOf(vararg bytes: UByte): ByteArray = bytes.toByteArray()
}