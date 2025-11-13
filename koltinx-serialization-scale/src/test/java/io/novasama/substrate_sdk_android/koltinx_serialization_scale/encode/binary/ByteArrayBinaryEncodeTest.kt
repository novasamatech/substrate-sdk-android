package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode.binary

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.BinaryScale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.FixedLength
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.WithLength20
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.encodeToByteArray
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers.ByteArraySerializable
import io.novasama.substrate_sdk_android.scale.dataType.byteArray
import io.novasama.substrate_sdk_android.scale.dataType.string
import io.novasama.substrate_sdk_android.scale.dataType.toByteArray
import kotlinx.serialization.Serializable
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class ByteArrayBinaryEncodeTest : BinaryEncodeTest() {

    @Test
    fun `should encode fixed byte array from annotation`() {
        @Serializable
        class TestData(@FixedLength(20) val bytes: ByteArray)

        val value = ByteArray(20) { it.toByte() }
        val result = BinaryScale.encodeToByteArray<TestData>(TestData(value))
        assertArrayEquals(value, result)
    }

    @Test
    fun `should encode variable length byte array`() {
        val value = ByteArray(25) { it.toByte() }
        val expected = byteArray.toByteArray(value)
        val result = BinaryScale.encodeToByteArray<ByteArray>(value)
        assertArrayEquals(expected, result)
    }

    @JvmInline
    @Serializable
    value class ByteArraySerializableTestData(val a: ByteArraySerializable)

    @Test
    fun `should keep compatibility with ByteArraySerializable`() {
        val value = ByteArray(25) { it.toByte() }
        val expected = byteArray.toByteArray(value)

        val result = BinaryScale.encodeToByteArray<ByteArraySerializableTestData>(ByteArraySerializableTestData(value))
        assertArrayEquals(expected, result)
    }

    @Test
    fun `should encode string`() {
        val value = "Test"
        val expected = string.toByteArray(value)

        runEncodeTest(value = value, expected = expected)
    }

    @Test
    fun `WithFixedLength wrapper works`() {
        val bytes = ByteArray(20) { 1 }
        val result = BinaryScale.encodeToByteArray<WithLength20<ByteArray>>(WithLength20(bytes))

        assertArrayEquals(bytes, result)
    }
}