package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encode

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.Scale
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encodeToDynamicStructure
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
