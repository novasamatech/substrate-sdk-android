package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encode

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Serializable
import org.junit.Test

class SetTypeTests: EncodeTest() {

    @Test
    fun `should encode set of strings`() = runEncodeTest(
        value = setOf("1", "2", "3"),
        expected = setOf("1", "2", "3")
    )

    @Test
    fun `should encode set of strings in struct`() {
        @Serializable
        data class Entry(val a: String, val b: Set<String>)

        runEncodeTest(
            value = Entry("1", setOf("1", "2", "3")),
            expected = Struct.Instance(
                mapOf(
                    "a" to "1",
                    "b" to setOf("1", "2", "3")
                )
            )
        )
    }
}
