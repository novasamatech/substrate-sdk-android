package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode.binary

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.BinaryScale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decodeFromByteArray
import org.junit.Assert

open class BinaryDecodeTest {

    inline fun <reified T> runDecodeTest(raw: ByteArray, expected: T) {
        val result: T = BinaryScale.decodeFromByteArray<T>(raw)

        Assert.assertEquals(expected, result)
    }
}