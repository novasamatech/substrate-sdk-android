@file:UseSerializers(BigIntegerSerializer::class)

package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.decode

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.serializers.BigIntegerSerializer
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.junit.Test

@Serializable
sealed class Sealed {

    @Serializable
    object A : Sealed()

    @Serializable
    data class B(val a: Boolean, val b: String) : Sealed()

    @Serializable
    data class Single(val element: String) : Sealed()

    @Serializable
    data class SingleList(val elements: List<String>) : Sealed()
}

class EnumTest : DecodeTest() {

    @Test
    fun `should decode variant object`() = runDecodeTest(
        expected = Sealed.A as Sealed,
        raw = DictEnum.Entry("A", null)
    )

    @Test
    fun `should decode variant struct`() = runDecodeTest(
        expected = Sealed.B(true, "3") as Sealed,
        raw = DictEnum.Entry("B", Struct.Instance(mapOf("a" to true, "b" to "3")))
    )

    @Test
    fun `should decode variant value`() = runDecodeTest(
        expected = Sealed.Single("a") as Sealed,
        raw = DictEnum.Entry("Single", "a")
    )

    @Test
    fun `should decode variant value list`() = runDecodeTest(
        expected = Sealed.SingleList(listOf("a", "b")) as Sealed,
        raw = DictEnum.Entry("SingleList", listOf("a", "b"))
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

        runDecodeTest(
            expected = SomeStruct(e1 = Sealed.Single("s")),
            raw = Struct.Instance(
                mapOf(
                    "e1" to DictEnum.Entry("Single", "s"),
                )
            )
        )

        runDecodeTest(
            expected = SomeStruct(e1 = Sealed.B(true, "b")),
            raw = Struct.Instance(
                mapOf(
                    "e1" to DictEnum.Entry(
                        "B", Struct.Instance(
                            mapOf(
                                "a" to true,
                                "b" to "b"
                            )
                        )
                    ),
                )
            )
        )
    }
}
