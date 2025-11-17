package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode.binary

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.FixedLength
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.WithLength20
import io.novasama.substrate_sdk_android.runtime.definitions.types.useScaleWriter
import io.novasama.substrate_sdk_android.scale.dataType.boolean
import kotlinx.serialization.Serializable
import org.junit.Test

class ListDecodeTest : BinaryDecodeTest() {

    @Test
    fun `should encode variable-length list`() {
        val (expected, encoded) = variableLengthExpendedAndEncoded()
        runDecodeTest(encoded, expected)
    }

    @Test
    fun `should encode variable-length list as element`() {
        @Serializable
        data class TestData(val list: List<Boolean>)

        val (expected, encoded) = variableLengthExpendedAndEncoded()
        runDecodeTest(encoded, TestData(expected))
    }

    @Test
    fun `should encode fixed-length list`() {
        val (expected, encoded) = fixedLengthExpendedAndEncoded(20)
        runDecodeTest(encoded, WithLength20(expected))
    }

    @Test
    fun `should encode fixed-length list as element`() {

        @Serializable
        data class TestData(@FixedLength(20) val list: List<Boolean>)

        val (expected, encoded) = fixedLengthExpendedAndEncoded(20)
        runDecodeTest(encoded, TestData(expected))
    }

    private fun variableLengthExpendedAndEncoded(): Pair<List<Boolean>, ByteArray> {
        val expected = listOf(true, false)
        val encoded = useScaleWriter {
            writeCompact(expected.size)
            expected.forEach { boolean.write(this, it) }
        }

        return expected to encoded
    }

    private fun fixedLengthExpendedAndEncoded(size: Int): Pair<List<Boolean>, ByteArray> {
        val expected = (0 until  size).map { true }
        val encoded = useScaleWriter {
            expected.forEach { boolean.write(this, it) }
        }

        return expected to encoded
    }
}