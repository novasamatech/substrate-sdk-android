package io.novasama.substrate_sdk_android.extensions

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class NamingConversionsTest {

    @Test
    fun `should convert snake case to camel case`() {
        runToCamelCaseTest("", "")
        runToCamelCaseTest("test", "test")
        runToCamelCaseTest("one_two", "oneTwo")
        runToCamelCaseTest("one_two_three", "oneTwoThree")
    }

    @Test
    fun `snake case to camel case should ignore locale`() {
        // turkish languages is one of those who has exotic rules for letter capitalization, specifically for letter 'i'
        Locale.setDefault(Locale("tr"))

        runToCamelCaseTest("fund_index", "fundIndex")
    }

    @Test
    fun `should convert camel case to snake case`() {
        runToSnakeCaseTest("", "")
        runToSnakeCaseTest("test", "test")
        runToSnakeCaseTest("oneTwo", "one_two")
        runToSnakeCaseTest("oneTwoThree", "one_two_three")
    }

    @Test
    fun `snakeCaseToCamelCase should not modify already present camel case`() {
        runToCamelCaseTest("oneTwoThree", "oneTwoThree")
    }

    @Test
    fun `camelCaseToSnakeCase should not modify already present snake case`() {
        runToSnakeCaseTest("one_two_three", "one_two_three")
    }

    @Test
    fun `camel case to snake case should ignore locale`() {
        // turkish languages is one of those who has exotic rules for letter capitalization, specifically for letter 'i'
        Locale.setDefault(Locale("tr"))

        runToSnakeCaseTest("fundIndex", "fund_index")
    }

    private fun runToCamelCaseTest(
        origin: String,
        expected: String
    ) {
        assertEquals(expected, origin.snakeCaseToCamelCase())
    }

    private fun runToSnakeCaseTest(
        origin: String,
        expected: String
    ) {
        assertEquals(expected, origin.camelCaseToSnakeCase())
    }
}