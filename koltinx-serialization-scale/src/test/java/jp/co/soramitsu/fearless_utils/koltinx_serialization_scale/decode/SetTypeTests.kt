package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.decode

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Serializable
import org.junit.Test

class SetTypeTests: DecodeTest() {

    @Test
    fun `should decode set of strings`() = runDecodeTest(
        raw = setOf("1", "2", "3"),
        expected = setOf("1", "2", "3")
    )

    @Test
    fun `should decode set of strings inside struct`() {

        @Serializable
        data class Entry(val a: String, val b: Set<String>)

        runDecodeTest(
            expected = Entry("1", setOf("1", "2", "3")),
            raw = Struct.Instance(
                mapOf(
                    "a" to "1",
                    "b" to setOf("1", "2", "3")
                )
            )
        )
    }
}
