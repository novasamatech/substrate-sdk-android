package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Serializable
import org.junit.Test

@JvmInline
@Serializable
value class InlineBool(val value: Boolean)

@Serializable
data class StructWithInlineField(val field: InlineBool)

class InlineTest : DecodeTest() {


    @Test
    fun `should decode inline value`() = runDecodeTest(
        raw = true,
        expected = InlineBool(true)
    )

    @Test
    fun `should decode inline value in nested structure`() = runDecodeTest(
        expected = StructWithInlineField(field = InlineBool(true)),
        raw = Struct.Instance(mapOf("field" to true))
    )
}