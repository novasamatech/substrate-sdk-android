package io.novasama.substrate_sdk_android.wsrpc.request.runtime

import io.novasama.substrate_sdk_android.common.assertThrows
import org.junit.Assert.*
import org.junit.Test
import java.lang.IllegalArgumentException

class UnsubscribeMethodResolverTest {

    @Test
    fun `should resolve storage subscription`() {
        performTest("state_subscribeStorage", "state_unsubscribeStorage")
    }

    @Test
    fun `should resolve runtime version subscription`() {
        performTest("state_subscribeRuntimeVersion", "state_unsubscribeRuntimeVersion")
    }

    @Test
    fun `should resolve call chain group`() {
        performTest("chain_subscribeAllHeads", "chain_unsubscribeAllHeads")
    }

    @Test
    fun `should throw on non-subscribe method`() {
        assertThrows<IllegalArgumentException> {
            UnsubscribeMethodResolver.resolve("state_getStorage")
        }
    }

    private fun performTest(subscribeName: String, expectedUnsubscribeName : String) {
        assertEquals(expectedUnsubscribeName, UnsubscribeMethodResolver.resolve(subscribeName))
    }
}