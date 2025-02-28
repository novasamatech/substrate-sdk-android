package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.Scale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode
import org.junit.Assert

open class DecodeTest {

    inline fun <reified T> runDecodeTest(
        raw: Any?,
        expected: T
    ) {
        val result: T = Scale.decode<T>(raw)

        Assert.assertEquals(expected, result)
    }
}