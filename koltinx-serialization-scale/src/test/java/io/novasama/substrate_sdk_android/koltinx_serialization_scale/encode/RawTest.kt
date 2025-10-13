package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers.AsRawScaleValue
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers.RawScaleValue
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import kotlinx.serialization.Serializable
import org.junit.Test

class RawTest : EncodeTest() {

    @Serializable
    @JvmInline
    value class WithRaw(val raw: RawScaleValue)

    @Test
    fun `should encode raw value in property`() = runEncodeTest(
        expected = DictEnum.Entry("test", 1),
        value = WithRaw(DictEnum.Entry("test", 1))
    )

    @Test
    fun `should decode raw value top level`() = runEncodeTest(
        expected = DictEnum.Entry("test", 1),
        value = AsRawScaleValue(DictEnum.Entry("test", 1))
    )
}