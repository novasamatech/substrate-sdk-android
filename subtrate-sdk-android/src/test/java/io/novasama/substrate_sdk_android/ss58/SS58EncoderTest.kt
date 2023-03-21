package io.novasama.substrate_sdk_android.ss58

import com.google.gson.Gson
import io.novasama.substrate_sdk_android.extensions.fromHex
import io.novasama.substrate_sdk_android.extensions.toHexString
import io.novasama.substrate_sdk_android.getResourceReader
import io.novasama.substrate_sdk_android.ss58.SS58Encoder.addressPrefix
import io.novasama.substrate_sdk_android.ss58.SS58Encoder.toAccountId
import io.novasama.substrate_sdk_android.ss58.SS58Encoder.toAddress
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SS58EncoderTest {

    val gson = Gson()

    @Test
    fun `should extract address prefix`() = runWithTestCases {
        assertEquals(it.addressPrefix.toShort(), it.address.addressPrefix())
    }

    @Test
    fun `should decode to accountId`() = runWithTestCases {
        assertEquals(
            it.publicKey,
            it.address.toAccountId().toHexString(withPrefix = true)
        )
    }

    @Test
    fun `should encode to address`() = runWithTestCases {
        assertEquals(
            it.address,
            it.publicKey.fromHex().toAddress(it.addressPrefix.toShort())
        )
    }

    private fun runWithTestCases(test: (TestCase) -> Unit) {
        val testCases = gson.fromJson(
            getResourceReader("ss58/ss58_tests.json"),
            Array<TestCase>::class.java
        )

        testCases.forEach(test)
    }

    private class TestCase(
        val address: String,
        val publicKey: String,
        val addressPrefix: Int
    )
}