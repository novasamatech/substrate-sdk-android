package jp.co.soramitsu.fearless_utils.wsrpc

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.any
import jp.co.soramitsu.fearless_utils.wsrpc.recovery.Reconnector
import jp.co.soramitsu.fearless_utils.wsrpc.request.RequestExecutor
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse
import jp.co.soramitsu.fearless_utils.wsrpc.subscription.response.SubscriptionChange
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SocketServiceTest {

    private val mockSocketFactory = MockSocketFactory()

    private val socketService: SocketService = SocketService(
        jsonMapper = Gson(),
        logger = StdoutLogger,
        webSocketFactory = mockSocketFactory,
        reconnector = Reconnector(),
        requestExecutor = RequestExecutor()
    )

    @Mock
    lateinit var listener: SocketService.ResponseListener<SubscriptionChange>

    @Test
    fun `subscribed callback should handle integer ids`() {
        val callback = socketService.SubscribedCallback(
            initiatorId = 123,
            unsubscribeMethod = "test",
            subscriptionCallback = listener
        )

        callback.onNext(RpcResponse("2.0", result = 2595588254652828, id = 123, error = null))

        verify(listener, never()).onError(any())
    }

    @Test
    fun `subscribed callback should handle string ids`() {
        val callback = socketService.SubscribedCallback(
            initiatorId = 123,
            unsubscribeMethod = "test",
            subscriptionCallback = listener
        )

        callback.onNext(RpcResponse("2.0", result = "n6in3VIm96u3ABQE", id = 123, error = null))

        verify(listener, never()).onError(any())
    }
}