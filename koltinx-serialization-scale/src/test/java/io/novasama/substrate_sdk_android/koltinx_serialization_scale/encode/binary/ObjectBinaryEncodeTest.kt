package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode.binary

import kotlinx.serialization.Serializable
import org.junit.Test

class ObjectBinaryEncodeTest : BinaryEncodeTest() {

    @Serializable
    object TestData

    @Test
    fun `should encode object`() {
        runEncodeTest(value = TestData, expected = byteArrayOf())
    }
}