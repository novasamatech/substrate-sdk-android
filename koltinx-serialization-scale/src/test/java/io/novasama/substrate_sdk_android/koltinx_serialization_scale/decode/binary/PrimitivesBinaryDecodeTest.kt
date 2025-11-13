package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode.binary

import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u8
import io.novasama.substrate_sdk_android.runtime.definitions.types.useScaleWriter
import io.novasama.substrate_sdk_android.scale.dataType.uint8
import kotlinx.serialization.Serializable
import org.junit.Test

class PrimitivesBinaryDecodeTest : BinaryDecodeTest() {

    @Test
    fun `should decode boolean as element`() {
        @Serializable
        data class TestData(val a: Boolean)

        runDecodeTest(raw = byteArrayOf(0x01), expected = TestData(true))
        runDecodeTest(raw = byteArrayOf(0x00), expected = TestData(false))
    }

    @Test
    fun `should decode boolean as root`() {
        runDecodeTest(raw = byteArrayOf(0x01), expected = true)
        runDecodeTest(raw = byteArrayOf(0x00), expected = false)
    }

    @Test
    fun `should decode byte`() {
        runDecodeTest(raw = byteArrayOf(0x12), expected = 0x12.toByte())
    }

    @Test
    fun `should decode long`() {
        val expected: Long = 123
        val encoded = useScaleWriter { writeLong(expected) }
        runDecodeTest(raw = encoded, expected = expected)
    }

    @Test
    fun `should decode short`() {
        val expected: Short = 123
        val encoded = useScaleWriter { writeShort(expected) }
        runDecodeTest(raw = encoded, expected = expected)
    }

    @Test
    fun `should decode byte as element`() {
        @Serializable
        data class TestData(val a: Byte)

        runDecodeTest(raw = byteArrayOf(0x12), expected = TestData(0x12))
    }
}