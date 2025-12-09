package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode.binary

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.EnumIndex
import kotlinx.serialization.Serializable
import org.junit.Test

class DictEnumBinaryDecodeTest : BinaryDecodeTest() {

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

        @Serializable
        @EnumIndex(255)
        object LastPossibleIndex : Sealed()
    }

    @Test
    fun `should decode variant object`() = runDecodeTest(
        expected = Sealed.Null as Sealed,
        raw = byteArrayOf(0x00)
    )

    @Test
    fun `should decode variant value`() = runDecodeTest(
        expected = Sealed.Single(true) as Sealed,
        raw = byteArrayOf(0x01, 0x01)
    )

    @Test
    fun `should decode variant struct`() = runDecodeTest(
        expected = Sealed.Double(a = true, b = false) as Sealed,
        raw =  byteArrayOf(0x02, 0x01, 0x00)
    )

    @Test
    fun `should decode last possible index`() = runDecodeTest(
        expected = Sealed.LastPossibleIndex as Sealed,
        raw =  ubyteArrayOf(0xffu).toByteArray()
    )
}