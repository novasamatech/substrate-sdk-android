package io.novasama.substrate_sdk_android.runtime.definitions.types.primitives

import io.novasama.substrate_sdk_android.runtime.definitions.types.BaseTypeTest
import org.junit.Test

import io.novasama.substrate_sdk_android.runtime.definitions.types.toHex
import io.novasama.substrate_sdk_android.runtime.definitions.types.fromHex
import org.junit.Assert.assertEquals

class IntTypeTest : BaseTypeTest() {

    private val tests = listOf(
        "222222224" to "0x90d73e0d00000000",
        "333333335" to "0x5743de1300000000",
        "-7772020" to "0x8c6889ffffffffff"
    )

    @Test
    fun `should encode signed integers`() {
        tests.forEach { (numberStr, encodedHex) ->
            val actualEncoded = i64.toHex(runtime, numberStr.toBigInteger())

            assertEquals(encodedHex, actualEncoded)
        }
    }

    @Test
    fun `should decode signed integers`() {
        tests.forEach { (numberStr, encodedHex) ->
            val actualDecoded = i64.fromHex(runtime, encodedHex)

            assertEquals(numberStr.toBigInteger(), actualDecoded)
        }
    }
}