package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode.binary

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.EnumIndex
import kotlinx.serialization.Serializable
import org.junit.Test

class DictEnumBinaryEncodeTest : BinaryEncodeTest() {

    @Serializable
    sealed class Sealed {

        @Serializable
        @EnumIndex(0)
        object Null : Sealed()

        @Serializable
        @EnumIndex(1)
        data class Single(val element: Boolean) : Sealed()

        @Serializable
        @EnumIndex(2)
        data class Double(val a: Boolean, val b: Boolean) : Sealed()
    }

    @Test
    fun `should encode variant object`() = runEncodeTest(
        value = Sealed.Null as Sealed,
        expected = byteArrayOf(0x00)
    )

    @Test
    fun `should encode variant value`() = runEncodeTest(
        value = Sealed.Single(true) as Sealed,
        expected = byteArrayOf(0x01, 0x01)
    )

    @Test
    fun `should encode variant struct`() = runEncodeTest(
        value = Sealed.Double(a = true, b = false) as Sealed,
        expected =  byteArrayOf(0x02, 0x01, 0x00)
    )
}