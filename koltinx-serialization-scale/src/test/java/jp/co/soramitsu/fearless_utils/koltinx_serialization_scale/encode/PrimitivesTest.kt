package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encode

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
}
