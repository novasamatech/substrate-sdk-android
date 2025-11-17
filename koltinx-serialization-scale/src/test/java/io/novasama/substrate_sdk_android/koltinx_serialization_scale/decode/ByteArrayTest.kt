package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.Scale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers.ByteArraySerializable
import kotlinx.serialization.Serializable
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class ByteArrayTest : DecodeTest() {

    @Test
    fun `should decode byte array`() {
        val value = byteArrayOf(0x00, 0x01)
        val result = Scale.decode<ByteArray>(value)

        assertArrayEquals(value, result)
    }

    @JvmInline
    @Serializable
    value class TestData(val byteArray: ByteArraySerializable)

    @Test
    fun `should decode byte array as element`() {
        val value = byteArrayOf(0x00, 0x01)
        val result = Scale.decode<TestData>(value)

        assertArrayEquals(value, result.byteArray)
    }
}