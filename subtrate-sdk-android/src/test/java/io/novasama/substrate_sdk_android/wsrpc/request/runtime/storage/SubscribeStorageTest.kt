package io.novasama.substrate_sdk_android.wsrpc.request.runtime.storage

import io.novasama.substrate_sdk_android.common.assertThrows
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.createFakeChange
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.IllegalArgumentException

class SubscribeStorageTest {

    @Test
    fun `should transform valid storage change`() {
        val change = createFakeChange(
            mapOf(
                "block" to "block",
                "changes" to listOf(listOf("key", "change"))
            )
        )

        val storageChange = change.storageChange()

        assertEquals("block", storageChange.block)
        assertEquals("change", storageChange.getSingleChange())
    }

    @Test
    fun `should throw on invalid storage change`() {
        val change = createFakeChange(
            mapOf(
                "block" to "block",
                "changes" to 1
            )
        )

        assertThrows<IllegalArgumentException> {
            change.storageChange()
        }
    }
}