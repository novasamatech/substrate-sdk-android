package io.novasama.substrate_sdk_android.runtime.definitions.types.composite

import io.novasama.substrate_sdk_android.runtime.definitions.types.BaseTypeTest
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.fromHex
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.BooleanType
import io.novasama.substrate_sdk_android.runtime.definitions.types.toHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VecTest : BaseTypeTest() {
    private val typeInstance = listOf(
        true, false, true, true
    )

    private val type = Vec(
        "test",
        TypeReference(BooleanType)
    )

    private val inHex = "0x1001000101"

    @Test
    fun `should decode instance`() {
        val decoded = type.fromHex(runtime, inHex)

        assertEquals(typeInstance, decoded)
    }

    @Test
    fun `should encode instance`() {
        val encoded = type.toHex(runtime, typeInstance)

        assertEquals(inHex, encoded)
    }

    @Test
    fun `should validate instance`() {
        assertTrue(type.isValidInstance(typeInstance))
        assertTrue(type.isValidInstance(listOf(false)))
        assertTrue(type.isValidInstance(listOf(false, true)))

        assertFalse(type.isValidInstance(listOf(1)))
        assertFalse(type.isValidInstance(1))
    }
}