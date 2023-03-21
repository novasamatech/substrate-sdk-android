package io.novasama.substrate_sdk_android.integration.author

import io.novasama.substrate_sdk_android.integration.BaseIntegrationTest
import io.novasama.substrate_sdk_android.wsrpc.executeAsync
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.author.PendingExtrinsicsRequest
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@Ignore("Manual run only")
class PendingExtrinsicsTest : BaseIntegrationTest() {

    @Test
    fun `should get pending extrinsics`() = runBlocking {
        val request = PendingExtrinsicsRequest()

        val result = socketService.executeAsync(request)

        print(result)

        assert(result.result is List<*>)
    }
}
