package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode.binary

import kotlinx.serialization.Serializable
import org.junit.Test

class ValueClassBinaryDecodeTest : BinaryDecodeTest() {

    @JvmInline
    @Serializable
    value class TestData(val a: Byte)

    @Test
    fun `should decode value class`() {
        runDecodeTest(raw = byteArrayOf(0x12), expected = TestData(0x12))
    }
}