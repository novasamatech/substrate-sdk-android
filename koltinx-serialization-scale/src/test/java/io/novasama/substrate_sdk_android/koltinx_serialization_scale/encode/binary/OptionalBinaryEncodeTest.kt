package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode.binary

import kotlinx.serialization.Serializable
import org.junit.Test

class OptionalBinaryEncodeTest : BinaryEncodeTest() {

    @Test
    fun `should encode optional value as root`() {
        runEncodeTest<Byte?>(value = null, expected = byteArrayOf(0x00))
        runEncodeTest<Byte?>(value = 0x12, expected = byteArrayOf(0x01, 0x12))
    }

    @Test
    fun `should encode optional value as element`() {
        @Serializable
        data class TestData(val a: Byte?)

        runEncodeTest<TestData>(value = TestData(null), expected = byteArrayOf(0x00))
        runEncodeTest<TestData>(value = TestData(0x12), expected = byteArrayOf(0x01, 0x12))
    }

    @Test
    fun `should encode optional boolean value`() {
        runEncodeTest<Boolean?>(value = null, expected = byteArrayOf(0x00))
        runEncodeTest<Boolean?>(value = false, expected = byteArrayOf(0x01))
        runEncodeTest<Boolean?>(value = true, expected = byteArrayOf(0x02))
    }

    @Test
    fun `should encode optional boolean as element`() {
        @Serializable
        data class TestData(val a: Boolean?)

        runEncodeTest<TestData>(value = TestData(null), expected = byteArrayOf(0x00))
        runEncodeTest<TestData>(value = TestData(false), expected = byteArrayOf(0x01))
        runEncodeTest<TestData>(value = TestData(true), expected = byteArrayOf(0x02))
    }
}