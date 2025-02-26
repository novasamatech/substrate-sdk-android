package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Serializable
import org.junit.Test

@Serializable
sealed class NullableEnum {

    @Serializable
    data class A(val option: Boolean?): NullableEnum()
}

class NullableTest : DecodeTest() {

    @Test
    fun `should decode nullable top level values`() {
        runDecodeTest(
            expected = null as Boolean?,
            raw = null
        )

        runDecodeTest(
            expected = true as Boolean?,
            raw = true
        )
    }

    @Test
    fun `should decode nullable values in struct`() {
        @Serializable
        data class TestStruct(val a: Boolean?)

        runDecodeTest(
            expected = TestStruct(a = null),
            raw = Struct.Instance(mapOf("a" to null))
        )

        runDecodeTest(
            expected = TestStruct(a = true),
            raw = Struct.Instance(mapOf("a" to true))
        )
    }

    @Test
    fun `should decode null inside enum`() {
        runDecodeTest(
            expected = NullableEnum.A(option = null) as NullableEnum,
            raw = DictEnum.Entry("A", null)
        )

        runDecodeTest(
            expected = NullableEnum.A(option = true) as NullableEnum,
            raw = DictEnum.Entry("A", true)
        )
    }
}