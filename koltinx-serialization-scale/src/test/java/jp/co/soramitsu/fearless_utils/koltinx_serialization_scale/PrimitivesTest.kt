package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale

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
}
