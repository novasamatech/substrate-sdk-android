package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.Scale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations.AsTuple
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.BinaryScale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.EnumIndex
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decodeFromByteArray
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Test

class MixedEncoderTypesTest {

    @Serializable
    sealed interface SealedWithMixedAnnotations {

        @Serializable
        @AsTuple
        @EnumIndex(0)
        data class A(val a: Boolean): SealedWithMixedAnnotations
    }

    @Test
    fun `should decode struct in both formats`(){
        val fromBinary = BinaryScale.decodeFromByteArray<SealedWithMixedAnnotations>( byteArrayOf(0x00, 0x01))
        val fromRuntime = Scale.decode<SealedWithMixedAnnotations>(DictEnum.Entry("A", listOf(true)))

        val expected = SealedWithMixedAnnotations.A(true)

        assertEquals(fromRuntime, expected)
        assertEquals(fromBinary, expected)
    }
}