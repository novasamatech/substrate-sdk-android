package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encode

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import org.junit.Test
import kotlinx.serialization.Serializable

class ListTest : EncodeTest() {

    @Test
    fun `should encode list of primitives`() = runEncodeTest(
        value = listOf(1, 2, 3),
        expected = listOf(1, 2, 3).map { it.toBigInteger() }
    )

    @Test
    fun `should encode list of composite types`() {

        @Serializable
        class Entry(val a: Int, val b: Boolean)

        runEncodeTest(
            value = listOf(
                Entry(1, true),
                Entry(2, false)
            ),
            expected = listOf(
                Struct.Instance(mapOf("a" to 1.toBigInteger(), "b" to true)),
                Struct.Instance(mapOf("a" to 2.toBigInteger(), "b" to false)),
            )
        )
    }

    @Test
    fun `should encode list inside struct`() {

        @Serializable
        data class Entry(val a: List<Boolean>)

        runEncodeTest(
            value = Entry(listOf(true, false, true)),
            expected = Struct.Instance(
                mapOf(
                    "a" to listOf(true, false, true)
                )
            )
        )
    }
}