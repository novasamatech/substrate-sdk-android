package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encode

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.Scale
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encodeToDynamicStructure
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.junit.Assert
import org.junit.Test
import java.math.BigInteger

@Serializable
sealed class Sealed {

    @Serializable
    object A : Sealed()

    @Serializable
    class B(val a: Boolean, val b: String) : Sealed()

    @Serializable
    class Single(val element: String) : Sealed()

    @Serializable
    class SingleList(val elements: List<String>) : Sealed()
}

@Serializable
sealed class MultiAddress {

    @Serializable
    class Id(val value: ByteArray) : MultiAddress()

    @Serializable
    class Index(@Contextual val value: BigInteger) : MultiAddress()

    @Serializable
    class Raw(val value: ByteArray) : MultiAddress()

    @Serializable
    class Address32(val value: ByteArray) : MultiAddress() {
        init {
            require(value.size == 32) {
                "Address32 should be 32 bytes long"
            }
        }
    }

    @Serializable
    class Address20(val value: ByteArray) : MultiAddress() {
        init {
            require(value.size == 20) {
                "Address20 should be 20 bytes long"
            }
        }
    }
}

class EnumTests : EncodeTest() {

    @Test
    fun `should encode variant object`() = runEncodeTest(
        value = Sealed.A as Sealed,
        expected = DictEnum.Entry("A", null)
    )

    @Test
    fun `should encode variant struct`() = runEncodeTest(
        value = Sealed.B(true, "3") as Sealed,
        expected = DictEnum.Entry("B", Struct.Instance(mapOf("a" to true, "b" to "3")))
    )

    @Test
    fun `should encode variant value`() = runEncodeTest(
        value = Sealed.Single("a") as Sealed,
        expected = DictEnum.Entry("Single", "a")
    )

    @Test
    fun `should encode variant value list`() = runEncodeTest(
        value = Sealed.SingleList(listOf("a", "b")) as Sealed,
        expected = DictEnum.Entry("SingleList", listOf("a", "b"))
    )

    @Test
    fun `should encode multiaddress`() {
        val value = byteArrayOf(0x00, 0x01)
        val result = Scale.encodeToDynamicStructure<MultiAddress>(MultiAddress.Id(value))

        require(result is DictEnum.Entry<*>)

        Assert.assertArrayEquals(value, result.value as ByteArray)
    }

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

        runEncodeTest(
            value = SomeStruct(e1 = Sealed.Single("s")),
            expected = Struct.Instance(
                mapOf(
                    "e1" to DictEnum.Entry("Single", "s"),
                )
            )
        )

        runEncodeTest(
            value = SomeStruct(e1 = Sealed.B(true, "b")),
            expected = Struct.Instance(
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
