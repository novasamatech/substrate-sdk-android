package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.Scale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode
import org.junit.Assert

open class EncodeTest {

    inline fun <reified T> runEncodeTest(
        value: T,
        expected: Any?
    ) {
        val result = Scale.encode(value)

        Assert.assertEquals(expected, result)
    }
}