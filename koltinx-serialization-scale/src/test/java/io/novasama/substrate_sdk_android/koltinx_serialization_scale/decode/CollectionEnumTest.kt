package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Serializable
import org.junit.Test


class CollectionEnumTest : DecodeTest() {

    enum class TestEnum {
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
}