package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.decode

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.Scale
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encodeToDynamicStructure
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class PrimitivesTest : DecodeTest() {

    @Test
    fun `should decode number`() = runtDecodeTest(
        raw = 123.toBigInteger(),
        expected = 123.toBigInteger()
    )

    @Test
    fun `should decode string`() = runtDecodeTest(
        raw = "123",
        expected = "123"
    )

    @Test
    fun `should decode boolean`() = runtDecodeTest(
        raw = true,
        expected = true
    )

    @Test
    fun `should decode byte array`() {
        val value = byteArrayOf(0x00, 0x01)
        val result = Scale.encodeToDynamicStructure(value)

        assertArrayEquals(value, result as ByteArray)
    }
}
