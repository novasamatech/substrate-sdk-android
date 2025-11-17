package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode.binary

import kotlinx.serialization.Serializable
import org.junit.Test

class ObjectBinaryDecodeTest : BinaryDecodeTest() {

    @Serializable
    object TestData

    @Test
    fun `should decode value class`() {
        runDecodeTest(raw = byteArrayOf(), expected = TestData)
    }
}