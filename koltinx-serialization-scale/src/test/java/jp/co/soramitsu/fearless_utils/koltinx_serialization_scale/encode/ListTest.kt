package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encode

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Serializable
import org.junit.Test

class ListTest : EncodeTest() {

    @Test
    fun `should encode list of primitives`() = runEncodeTest(
        value = listOf("1", "2", "3"),
        expected = listOf("1", "2", "3")
    )

    @Test
    fun `should encode list of composite types`() {

        @Serializable
        class Entry(val a: String, val b: Boolean)

        runEncodeTest(
            value = listOf(
                Entry("1", true),
                Entry("2", false)
            ),
            expected = listOf(
                Struct.Instance(mapOf("a" to "1", "b" to true)),
                Struct.Instance(mapOf("a" to "2", "b" to false)),
            )
        )
    }

    @Test
    fun `should encode list inside struct`() {

        @Serializable
        data class Entry(val a: String, val b: List<String>)

        runEncodeTest(
            value = Entry("1", listOf("1", "2", "3")),
            expected = Struct.Instance(
                mapOf(
                    "a" to "1",
                    "b" to listOf("1", "2", "3")
                )
            )
        )
    }
}
