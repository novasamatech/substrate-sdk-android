package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode.binary

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.BinaryScale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.EnumIndex
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decodeFromByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.junit.Test

class EnumBinaryDecodeTest : BinaryDecodeTest() {

    @Serializable
    enum class TestData {
        A, B, C, D
    }

    @Test
    fun `should decode enum entry`() {
        runDecodeTest(byteArrayOf(0x00), TestData.A)
        runDecodeTest(byteArrayOf(0x01), TestData.B)
        runDecodeTest(byteArrayOf(0x02), TestData.C)
    }

    @Test(expected = SerializationException::class)
    fun `should throw for unknown variant`() {
        BinaryScale.decodeFromByteArray<TestData>(byteArrayOf(0x05))
    }

    @Serializable
    enum class TestDataEnumIndex {
        @EnumIndex(2)
        A
    }

    @Test
    fun `should decode enum entry with custom index`() {
        runDecodeTest(byteArrayOf(0x02), TestDataEnumIndex.A)
    }

    @Serializable
    enum class TestDataMismatchingIndices {
        @EnumIndex(2)
        A,
        @EnumIndex(1)
        B,
        @EnumIndex(0)
        C,

        @EnumIndex(255)
        LAST_POSSIBLE_INDEX
    }

    @Test
    fun `should decode enum entry with mismatching indices`() {
        runDecodeTest(byteArrayOf(0x02), TestDataMismatchingIndices.A)
        runDecodeTest(byteArrayOf(0x01), TestDataMismatchingIndices.B)
        runDecodeTest(byteArrayOf(0x00), TestDataMismatchingIndices.C)
    }

    @Test
    fun `should decode last possible index`() {
        runDecodeTest(ubyteArrayOf(0xffu).toByteArray(), TestDataMismatchingIndices.LAST_POSSIBLE_INDEX)
    }
}