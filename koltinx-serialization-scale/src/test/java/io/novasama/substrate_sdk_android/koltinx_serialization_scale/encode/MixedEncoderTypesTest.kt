package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.Scale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations.AsTuple
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.BinaryScale
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.EnumIndex
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decodeFromByteArray
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.encodeToByteArray
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import kotlinx.serialization.Serializable
import org.junit.Assert.assertArrayEquals
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
    fun `should encode struct in both formats`(){
        val input = SealedWithMixedAnnotations.A(true)
        val expectedBinary = byteArrayOf(0x00, 0x01)
        val expectedRuntime = DictEnum.Entry("A", listOf(true))

        val actualBinary = BinaryScale.encodeToByteArray<SealedWithMixedAnnotations>(input)
        val actualRuntime = Scale.encode<SealedWithMixedAnnotations>(input)

        assertArrayEquals(expectedBinary, actualBinary)
        assertEquals(expectedRuntime, actualRuntime)
    }
}