package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Serializable
import org.junit.Test

@Serializable
sealed class Sealed {

    @Serializable
    object A : Sealed()

    @Serializable
    class B(val a: Boolean, val b: Boolean) : Sealed()

    @Serializable
    class Single(val element: Boolean) : Sealed()

    @Serializable
    class SingleList(val elements: List<Boolean>) : Sealed()
}

class EnumTests : EncodeTest() {

    @Test
    fun `should encode variant object`() = runEncodeTest(
        value = Sealed.A as Sealed,
        expected = DictEnum.Entry("A", null)
    )

    @Test
    fun `should encode variant struct`() = runEncodeTest(
        value = Sealed.B(true, false) as Sealed,
        expected = DictEnum.Entry("B", Struct.Instance(mapOf("a" to true, "b" to false)))
    )

    @Test
    fun `should encode variant with single value`() = runEncodeTest(
        value = Sealed.Single(false) as Sealed,
        expected = DictEnum.Entry("Single", false)
    )

    @Test
    fun `should encode variant value list`() = runEncodeTest(
        value = Sealed.SingleList(listOf(true, false)) as Sealed,
        expected = DictEnum.Entry("SingleList", listOf(true, false))
    )

    @Test
    fun `should encode enum in struct`() {

        @Serializable
        class SomeStruct(val e1: Sealed)

        runEncodeTest(
            value = SomeStruct(e1 = Sealed.A),
            expected = Struct.Instance(
                mapOf(
                    "e1" to DictEnum.Entry("A", null),
                )
            )
        )
    }
}