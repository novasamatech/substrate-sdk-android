package io.novasama.substrate_sdk_android.extensions

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class SnakeToCamelCaseTest {

    @Test
    fun shouldPerformConversion() {
        runTest("", "")
        runTest("test", "test")
        runTest("one_two", "oneTwo")
        runTest("one_two_three", "oneTwoThree")
    }

    @Test
    fun shouldIgnoreLocale() {
        // turkish languages is one of those who has exotic rules for letter capitalization, specifically for letter 'i'
        Locale.setDefault(Locale("tr"))

        runTest("fund_index", "fundIndex")
    }

    private fun runTest(
        origin: String,
        expected: String
    ) {
        assertEquals(expected, origin.snakeCaseToCamelCase())
    }
}