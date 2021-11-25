package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale

import org.junit.Assert

open class EncodeTest {

    inline fun <reified T> runEncodeTest(
        value: T,
        expected: Any?
    ) {
        val result = Scale.encodeToDynamicStructure(value)

        Assert.assertEquals(expected, result)
    }
}
