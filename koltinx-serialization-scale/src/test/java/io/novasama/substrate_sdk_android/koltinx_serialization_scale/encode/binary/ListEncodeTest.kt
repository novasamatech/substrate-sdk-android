package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode.binary

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.FixedLength
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.WithLength20
import io.novasama.substrate_sdk_android.runtime.definitions.types.useScaleWriter
import io.novasama.substrate_sdk_android.scale.dataType.boolean
import kotlinx.serialization.Serializable
import org.junit.Test

class ListEncodeTest : BinaryEncodeTest() {

    @Test
    fun `should encode variable-length list`() {
        val (value, expected) = variableLengthValueAndEncoded()
        runEncodeTest(value, expected)
    }

    @Test
    fun `should encode variable-length list as element`() {
        @Serializable
        data class TestData(val list: List<Boolean>)

        val (value, expected) = variableLengthValueAndEncoded()
        runEncodeTest(TestData(value), expected)
    }

    @Test
    fun `should encode fixed-length list`() {
        val (value, expected) = fixedLengthValueAndEncoded(20)
        runEncodeTest(WithLength20(value), expected)
    }

    @Test
    fun `should encode fixed-length list as element`() {

        @Serializable
        data class TestData(@FixedLength(20) val list: List<Boolean>)

        val (value, expected) = fixedLengthValueAndEncoded(20)
        runEncodeTest(TestData(value), expected)
    }

    private fun variableLengthValueAndEncoded(): Pair<List<Boolean>, ByteArray> {
        val value = listOf(true, false)
        val encoded = useScaleWriter {
            writeCompact(value.size)
            value.forEach { boolean.write(this, it) }
        }

        return value to encoded
    }

    private fun fixedLengthValueAndEncoded(size: Int): Pair<List<Boolean>, ByteArray> {
        val value = (0 until  size).map { true }
        val encoded = useScaleWriter {
            value.forEach { boolean.write(this, it) }
        }

        return value to encoded
    }
}