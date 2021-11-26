package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.decode

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.scale.dataType.list
import kotlinx.serialization.Serializable
import org.junit.Test
import java.math.BigInteger
import kotlin.math.exp

class ListTest: DecodeTest() {

    @Test
    fun `should encode list of primitives`() = runtDecodeTest(
        expected = listOf("1", "2", "3"),
        raw = listOf("1", "2", "3")
    )

    @Test
    fun `should encode list of composite types`() {

        @Serializable
        data class Entry(val a: String, val b: Boolean)

        runtDecodeTest(
            expected = listOf(
                Entry("1", true),
                Entry("2", false)
            ),
            raw = listOf(
                Struct.Instance(mapOf("a" to "1", "b" to true)),
                Struct.Instance(mapOf("a" to "2", "b" to false)),
            )
        )
    }

    @Test
    fun `should decode list inside struct`() {

        @Serializable
        data class Entry(val a: String, val b: List<String>)

        runtDecodeTest(
            expected = Entry("1", listOf("1", "2", "3")),
            raw = Struct.Instance(
                mapOf(
                    "a" to "1",
                    "b" to listOf("1", "2", "3")
                )
            )
        )
    }
}
