package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode.binary

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.BinaryScale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.FixedLengthBytes
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decodeFromByteArray
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.serializers.ScaleByteArray20
import io.novasama.substrate_sdk_android.runtime.definitions.types.useScaleWriter
import io.novasama.substrate_sdk_android.scale.utils.directWrite
import kotlinx.serialization.Serializable
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class ByteArrayBinaryTest : BinaryDecodeTest() {

    @Test
    fun `should decode fixed byte array from annotation`() {
        @Serializable
        class TestData(@FixedLengthBytes(20) val bytes: ByteArray)

        val value = ByteArray(20) { it.toByte() }
        val result = BinaryScale.decodeFromByteArray<TestData>(value)
        assertArrayEquals(value, result.bytes)
    }

    @Test
    fun `should decode fixed byte array from type alias`() {
        @Serializable
        class TestData(val list: List<ScaleByteArray20>)

        val fixedBytes20 = ByteArray(20) { it.toByte() }
        val data = useScaleWriter {
            writeCompact(1) // length of `list`
            directWrite(fixedBytes20) // single element of `list`
        }

        val result = BinaryScale.decodeFromByteArray<TestData>(data)
        assertArrayEquals(fixedBytes20, result.list.single())
    }
}