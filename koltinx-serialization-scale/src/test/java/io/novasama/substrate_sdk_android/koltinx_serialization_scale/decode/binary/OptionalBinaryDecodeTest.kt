package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode.binary

import kotlinx.serialization.Serializable
import org.junit.Test

class OptionalBinaryDecodeTest : BinaryDecodeTest() {

    @Test
    fun `should decode optional value as root`() {
        runDecodeTest<Byte?>(raw = byteArrayOf(0x00), expected = null)
        runDecodeTest<Byte?>(raw = byteArrayOf(0x01, 0x12), expected = 0x12)
    }

    @Test
    fun `should decode optional value as element`() {
        @Serializable
        data class TestData(val a: Byte?)

        runDecodeTest<TestData>(raw = byteArrayOf(0x00), expected = TestData(null))
        runDecodeTest<TestData>(raw = byteArrayOf(0x01, 0x12), expected = TestData(0x12))
    }

    @Test
    fun `should decode optional boolean value`() {
        runDecodeTest<Boolean?>(raw = byteArrayOf(0x00), expected = null)
        runDecodeTest<Boolean?>(raw = byteArrayOf(0x01), expected = true)
        runDecodeTest<Boolean?>(raw = byteArrayOf(0x02), expected = false)
    }

    @Test
    fun `should decode optional boolean as element`() {
        @Serializable
        data class TestData(val a: Boolean?)

        runDecodeTest<TestData>(raw = byteArrayOf(0x00), expected = TestData(null))
        runDecodeTest<TestData>(raw = byteArrayOf(0x01), expected = TestData(true))
        runDecodeTest<TestData>(raw = byteArrayOf(0x02), expected = TestData(false))
    }
}