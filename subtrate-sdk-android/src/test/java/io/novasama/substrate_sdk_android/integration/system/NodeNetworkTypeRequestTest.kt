package io.novasama.substrate_sdk_android.integration.system

import io.novasama.substrate_sdk_android.integration.BaseIntegrationTest
import io.novasama.substrate_sdk_android.wsrpc.executeAsync
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.system.NodeNetworkTypeRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@Ignore("Manual run only")
class NodeNetworkTypeRequestTest : BaseIntegrationTest() {

    @Test
    fun `should get node network type`() = runBlocking {
        val response = socketService.executeAsync(NodeNetworkTypeRequest())

        val type = response.result as String

        assertEquals("Kusama", type)
    }
}
