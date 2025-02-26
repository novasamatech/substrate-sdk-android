package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Serializable
import org.junit.Test


class CollectionEnumTest : EncodeTest() {

    enum class TestEnum {
        A, B, C
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
}