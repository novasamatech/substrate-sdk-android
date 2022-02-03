package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encode

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.Scale
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encodeToDynamicStructure
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class PrimitivesTest : EncodeTest() {

    @Test
    fun `should encode number`() = runEncodeTest(
        value = 123.toBigInteger(),
        expected = 123.toBigInteger()
    )

    @Test
    fun `should encode string`() = runEncodeTest(
        value = "123",
        expected = "123"
    )

    @Test
    fun `should encode boolean`() = runEncodeTest(
        value = true,
        expected = true
    )

    @Test
    fun `should encode byteArray`() {
        val value = byteArrayOf(0, 1, 2, 3)
        val result = Scale.encodeToDynamicStructure(value)

        assertArrayEquals(value, result as ByteArray)
    }
}
