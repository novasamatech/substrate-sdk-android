package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations.SerializedFallback
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.Test

@Serializable
@SerializedFallback("Unknown")
sealed class Sealed {

    @Serializable
    object A : Sealed()

    @Serializable
    data class B(val a: Boolean, val b: Int) : Sealed()

    @Serializable
    data class Single(val element: Boolean) : Sealed()

    @Serializable
    data class SingleList(val elements: List<Boolean>) : Sealed()

    @Serializable
    @SerialName("ChangedName")
    object Renamed: Sealed()

    @Serializable
    object Unknown : Sealed()
}

class EnumTest : DecodeTest() {

    @Test
    fun `should decode variant object`() = runDecodeTest(
        expected = Sealed.A as Sealed,
        raw = DictEnum.Entry("A", null)
    )

    @Test
    fun `should decode variant struct`() = runDecodeTest(
        expected = Sealed.B(true, 3) as Sealed,
        raw = DictEnum.Entry("B", Struct.Instance(mapOf("a" to true, "b" to 3.toBigInteger())))
    )

    @Test
    fun `should decode variant value`() = runDecodeTest(
        expected = Sealed.Single(true) as Sealed,
        raw = DictEnum.Entry("Single", true)
    )

    @Test
    fun `should decode variant value list`() = runDecodeTest(
        expected = Sealed.SingleList(listOf(true, false)) as Sealed,
        raw = DictEnum.Entry("SingleList", listOf(true, false))
    )

    @Test
    fun `should decode enum in struct`() {

        @Serializable
        data class SomeStruct(val e1: Sealed)

        runDecodeTest(
            expected = SomeStruct(e1 = Sealed.A),
            raw = Struct.Instance(
                mapOf(
                    "e1" to DictEnum.Entry("A", null),
                )
            )
        )
    }

    @Test
    fun `should decode fallback variant object`() = runDecodeTest(
        expected = Sealed.Unknown as Sealed,
        raw = DictEnum.Entry("SomethingElse", true)
    )

    @Test
    fun `serial name works`() = runDecodeTest(
        expected = Sealed.Renamed as Sealed,
        raw = DictEnum.Entry("ChangedName", null)
    )
}