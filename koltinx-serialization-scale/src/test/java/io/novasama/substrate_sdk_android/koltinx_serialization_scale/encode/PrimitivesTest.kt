package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.Scale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class PrimitivesTest : EncodeTest() {

    @Test
    fun `should encode number`() = runEncodeTest(
        value = 123.toBigInteger(),
        expected = 123.toBigInteger()
    )

    @Test
    fun `should encode string`() {
        val value = "123"
        val result = Scale.encode(value)

        assertArrayEquals(value.encodeToByteArray(), result as ByteArray)
    }

    @Test
    fun `should encode boolean`() = runEncodeTest(
        value = true,
        expected = true
    )

    @Test
    fun `should encode byteArray`() {
        val value = byteArrayOf(0, 1, 2, 3)
        val result = Scale.encode(value)

        assertArrayEquals(value, result as ByteArray)
    }
}