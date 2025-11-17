package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode.binary

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.BinaryScale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.encodeToByteArray
import org.junit.Assert

open class BinaryEncodeTest {

    inline fun <reified T> runEncodeTest(value: T, expected: ByteArray) {
        val result: ByteArray = BinaryScale.encodeToByteArray<T>(value)

        Assert.assertArrayEquals(expected, result)
    }
}