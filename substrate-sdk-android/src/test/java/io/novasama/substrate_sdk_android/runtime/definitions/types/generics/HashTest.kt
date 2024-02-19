package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.novasama.substrate_sdk_android.common.assertThrows
import org.junit.Assert.assertEquals
import org.junit.Test

class HashTest {

    @Test
    fun `should have valid name`() {
        val hash = Hash(256)

        assertEquals(hash.name, "H256")
    }

    @Test
    fun `should require integer bytes`() {
        assertThrows<IllegalArgumentException> {
            Hash(129)
        }
    }

    @Test
    fun `should have valid length in bytes`() {
        val hash = Hash(256)

        assertEquals(hash.length, 32)
    }
}