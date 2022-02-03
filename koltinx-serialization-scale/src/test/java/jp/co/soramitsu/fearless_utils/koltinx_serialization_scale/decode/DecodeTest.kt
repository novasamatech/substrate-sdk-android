package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.decode

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.Scale
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.decodeFromDynamicStructure
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encodeToDynamicStructure
import org.junit.Assert

open class DecodeTest {

    inline fun <reified T> runDecodeTest(
        raw: Any?,
        expected: T
    ) {
        val result: T = Scale.decodeFromDynamicStructure(raw)

        Assert.assertEquals(expected, result)
    }
}
