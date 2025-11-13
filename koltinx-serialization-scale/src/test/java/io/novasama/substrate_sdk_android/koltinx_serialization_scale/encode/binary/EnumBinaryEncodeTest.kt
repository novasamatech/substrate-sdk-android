package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode.binary

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.EnumIndex
import kotlinx.serialization.Serializable
import org.junit.Test

class EnumBinaryEncodeTest : BinaryEncodeTest() {

    @Serializable
    enum class TestData {
        A, B, C, D
    }

    @Test
    fun `should encode enum entry`() {
        runEncodeTest(TestData.A, byteArrayOf(0x00))
        runEncodeTest(TestData.B, byteArrayOf(0x01))
        runEncodeTest(TestData.C, byteArrayOf(0x02))
    }

    @Serializable
    enum class TestDataEnumIndex {
        @EnumIndex(2)
        A
    }

    @Test
    fun `should encode enum entry with custom index`() {
        runEncodeTest(TestDataEnumIndex.A, byteArrayOf(0x02))
    }

    @Serializable
    enum class TestDataMismatchingIndices {
        @EnumIndex(2)
        A,
        @EnumIndex(1)
        B,
        @EnumIndex(0)
        C
    }

    @Test
    fun `should encode enum entry with mismatching indices`() {
        runEncodeTest(TestDataMismatchingIndices.A, byteArrayOf(0x02))
        runEncodeTest(TestDataMismatchingIndices.B, byteArrayOf(0x01))
        runEncodeTest(TestDataMismatchingIndices.C, byteArrayOf(0x00))
    }
}