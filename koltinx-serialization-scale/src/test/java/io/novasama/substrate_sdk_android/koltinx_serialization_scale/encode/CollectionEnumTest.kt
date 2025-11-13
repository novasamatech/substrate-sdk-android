package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations.AsDictEnum
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode.CollectionEnumTest
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode.CollectionEnumTest.TestEnum2
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.Test


class CollectionEnumTest : EncodeTest() {

    @Serializable
    enum class TestEnum {
        A, B, @SerialName("c") C
    }

    @Test
    fun `should encode collection enum`() = runEncodeTest(
        value = TestEnum.A,
        expected = "A"
    )

    @Test
    fun `should encode collection enum in struct`() {

        @Serializable
        class TestStruct(val a: TestEnum)

        runEncodeTest(
            value = TestStruct(a = TestEnum.A),
            expected = Struct.Instance(mapOf("a" to "A"))
        )
    }

    @Test
    fun `SerialName works`() = runEncodeTest(
        value = TestEnum.C,
        expected = "c"
    )

    @Serializable
    @AsDictEnum
    enum class TestEnumDict {
        A, B, @SerialName("c") C
    }

    @Test
    fun `should encode collection enum to dict entry with null value`() {
        runEncodeTest(
            expected = DictEnum.Entry("A", null),
            value = TestEnumDict.A
        )
    }

    @Test
    fun `should respect custom names for dict entry encoding`() {
        runEncodeTest(
            expected = DictEnum.Entry("c", null),
            value = TestEnumDict.C
        )
    }
}