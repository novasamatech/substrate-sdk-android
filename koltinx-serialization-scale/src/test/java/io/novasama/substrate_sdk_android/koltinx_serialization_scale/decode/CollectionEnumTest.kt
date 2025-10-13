package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.Scale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations.SerializedFallback
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.Test


class CollectionEnumTest : DecodeTest() {

    @SerializedFallback("A")
    @Serializable
    enum class TestEnum {
        A, B, C
    }

    @Serializable
    enum class TestEnum2 {
        @SerialName("a") A, B, C
    }

    @Serializable
    enum class TestEnumNoFallback {
        A, B
    }

    @SerializedFallback("F")
    @Serializable
    enum class EnumWithWrongFallback {
        A, B, C
    }


    @Test
    fun `should decode collection enum`() = runDecodeTest(
        raw = "A",
        expected = TestEnum.A
    )

    @Test
    fun `should encode collection enum in struct`() {

        @Serializable
        data class TestStruct(val a: TestEnum)

        runDecodeTest(
            expected = TestStruct(a = TestEnum.A),
            raw = Struct.Instance(mapOf("a" to "A"))
        )
    }

    @Test
    fun `should decode collection enum with fallback`() = runDecodeTest(
        raw = "D",
        expected = TestEnum.A
    )


    @Test(expected = IllegalStateException::class)
    fun `should throw when cannot resolve fallback`() {
        Scale.decode<EnumWithWrongFallback>("D")
    }

    @Test(expected = IllegalStateException::class)
    fun `should throw when faced unknown value without fallback`() {
        Scale.decode<TestEnumNoFallback>("D")
    }

    @Test
    fun `SerialName works`() = runDecodeTest(
        raw = "a",
        expected = TestEnum2.A
    )

    @Test
    fun `should decode collection enum from dict entry with null value`() {
        runDecodeTest(
            expected = TestEnum.A,
            raw = DictEnum.Entry("A", null)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw when attempting to decode from dict enum with non null value`() {
        runDecodeTest(
            expected = TestEnum.A,
            raw = DictEnum.Entry("A", 1)
        )
    }

    @Test
    fun `should respect custom names for dict entry decoding`() {
        runDecodeTest(
            expected = TestEnum2.A,
            raw = DictEnum.Entry("a", null)
        )
    }
}