package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers.AsRawScaleValue
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers.RawScaleValue
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import kotlinx.serialization.Serializable
import org.junit.Test

class RawTest : DecodeTest() {

    @JvmInline
    @Serializable
    value class WithRaw(val raw: RawScaleValue)

    @Test
    fun `should decode raw value in property`() = runDecodeTest(
        expected = WithRaw(raw = DictEnum.Entry("test", 1)),
        raw = DictEnum.Entry("test", 1)
    )

    @Test
    fun `should decode raw value top level`() = runDecodeTest(
        expected = AsRawScaleValue(DictEnum.Entry("test", 1)),
        raw = DictEnum.Entry("test", 1)
    )
}