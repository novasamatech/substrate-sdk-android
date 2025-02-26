package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Serializable
import org.junit.Test


class NullableTest : EncodeTest() {

    @Test
    fun `should encode nullable top level values`() {
        runEncodeTest(
            value = null as Boolean?,
            expected = null
        )

        runEncodeTest(
            value = true as Boolean?,
            expected = true
        )
    }

    @Test
    fun `should encode nullable values in struct`() {
        @Serializable
        class TestStruct(val a: Boolean?)

        runEncodeTest(
            value = TestStruct(a = null),
            expected = Struct.Instance(mapOf("a" to null))
        )

        runEncodeTest(
            value = TestStruct(a = true),
            expected = Struct.Instance(mapOf("a" to true))
        )
    }
}