package io.novasama.substrate_sdk_android.wsrpc.request.runtime.chain

import io.novasama.substrate_sdk_android.common.assertThrows
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.createFakeChange
import org.junit.Assert.*
import org.junit.Test
import java.lang.IllegalArgumentException

class SubscribeRuntimeVersionTest {

    @Test
    fun `should transform valid runtime version change`() {
        val change = createFakeChange(
            mapOf(
                "specVersion" to 1.0,
                "transactionVersion" to 1.0
            )
        )

        val storageChange = change.runtimeVersionChange()

        assertEquals(1, storageChange.specVersion)
        assertEquals(1, storageChange.transactionVersion)
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
            change.runtimeVersionChange()
        }
    }
}