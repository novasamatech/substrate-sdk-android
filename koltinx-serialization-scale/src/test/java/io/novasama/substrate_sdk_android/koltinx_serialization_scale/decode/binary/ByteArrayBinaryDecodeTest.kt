package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode.binary

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.BinaryScale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.FixedLength
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.WithLength20
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decodeFromByteArray
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers.ByteArraySerializable
import io.novasama.substrate_sdk_android.scale.dataType.byteArray
import io.novasama.substrate_sdk_android.scale.dataType.string
import io.novasama.substrate_sdk_android.scale.dataType.toByteArray
import kotlinx.serialization.Serializable
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class ByteArrayBinaryDecodeTest : BinaryDecodeTest() {

    @Test
    fun `should decode fixed byte array from annotation`() {
        @Serializable
        class TestData(@FixedLength(20) val bytes: ByteArray)

        val value = ByteArray(20) { it.toByte() }
        val result = BinaryScale.decodeFromByteArray<TestData>(value)
        assertArrayEquals(value, result.bytes)
    }

    @Test
    fun `should decode variable length byte array`() {
        val value = ByteArray(25) { it.toByte() }
        val data = byteArray.toByteArray(value)
        val result = BinaryScale.decodeFromByteArray<ByteArray>(data)
        assertArrayEquals(value, result)
    }

    @JvmInline
    @Serializable
    value class ByteArraySerializableTestData(val a: ByteArraySerializable)

    @Test
    fun `should keep compatibility with ByteArraySerializable`() {
        val value = ByteArray(25) { it.toByte() }
        val data = byteArray.toByteArray(value)

        val result = BinaryScale.decodeFromByteArray<ByteArraySerializableTestData>(data)
        assertArrayEquals(value, result.a)
    }

    @Test
    fun `should decode string`() {
        val expected = "Test"
        val encoded = string.toByteArray(expected)

        runDecodeTest(raw =encoded, expected =expected)
    }

    @Test
    fun `WithFixedLength wrapper works`() {
        val bytes = ByteArray(20) { 1 }
        val result = BinaryScale.decodeFromByteArray<WithLength20<ByteArray>>(bytes)

        assertArrayEquals(bytes, result.value)
    }
}