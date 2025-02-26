package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decode

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.Serializable
import org.junit.Test

class ListTest: DecodeTest() {

    @Test
    fun `should encode list of primitives`() = runDecodeTest(
        expected = listOf(false, true, false),
        raw = listOf(false, true, false)
    )

    @Test
    fun `should encode empty list`() = runDecodeTest(
        expected = emptyList<Boolean>(),
        raw = emptyList<Boolean>()
    )

    @Test
    fun `should encode list of composite types`() {

        @Serializable
        data class Entry(val b: Boolean)

        runDecodeTest(
            expected = listOf(
                Entry(true),
                Entry( false)
            ),
            raw = listOf(
                Struct.Instance(mapOf("b" to true)),
                Struct.Instance(mapOf("b" to false)),
            )
        )
    }

    @Test
    fun `should decode list inside struct`() {

        @Serializable
        data class Entry(val a: Boolean, val b: List<Boolean>)

        runDecodeTest(
            expected = Entry(true, listOf(false, true, false)),
            raw = Struct.Instance(
                mapOf(
                    "a" to true,
                    "b" to listOf(false, true, false)
                )
            )
        )
    }
}