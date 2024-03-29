package io.novasama.substrate_sdk_android.runtime.definitions.types.composite

import io.novasama.substrate_sdk_android.runtime.definitions.types.BaseTypeTest
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.fromHex
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.BooleanType
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u8
import io.novasama.substrate_sdk_android.runtime.definitions.types.toHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StructTest : BaseTypeTest() {

    private val type = Struct(
        "test",
        mapping = linkedMapOf(
            "bool" to TypeReference(BooleanType),
            "u8" to TypeReference(u8),
        )
    )

    private val expectedInstance = linkedMapOf(
        "bool" to true,
        "u8" to 9.toBigInteger()
    )

    private val expectedInHex = "0x0109"

    @Test
    fun `should decode instance`() {
        val decoded = type.fromHex(runtime, expectedInHex)

        assertEquals(expectedInstance, decoded.mapping)
    }

    @Test
    fun `should encode instance`() {
        val encoded = type.toHex(runtime, Struct.Instance(expectedInstance))

        assertEquals(expectedInHex, encoded)
    }

    @Test
    fun `should validate instance`() {
        val instance = Struct.Instance(expectedInstance)

        assertTrue(type.isValidInstance(instance))

        assertFalse(type.isValidInstance(1))
        assertFalse(type.isValidInstance(mapOf<String, Any>()))
        assertFalse(type.isValidInstance(Struct.Instance(mapOf<String, Any>())))
    }
}