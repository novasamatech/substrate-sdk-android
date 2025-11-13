package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode.binary

import kotlinx.serialization.Serializable
import org.junit.Test

class ValueClassBinaryEncodeTest : BinaryEncodeTest() {

    @JvmInline
    @Serializable
    value class TestData(val a: Byte)

    @Test
    fun `should encode value class`() {
        runEncodeTest(value = TestData(0x12), expected = byteArrayOf(0x12))
    }
}