package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Serializable
import org.junit.Test

@JvmInline
@Serializable
value class InlineBool(val value: Boolean)

@Serializable
class StructWithInlineField(val field: InlineBool)

class InlineTest : EncodeTest() {


    @Test
    fun `should encode inline value`() = runEncodeTest(
        value = InlineBool(true),
        expected = true
    )

    @Test
    fun `should encode inline value in nested structure`() = runEncodeTest(
        value = StructWithInlineField(field = InlineBool(true)),
        expected = Struct.Instance(mapOf(
            "field" to true
        ))
    )
}