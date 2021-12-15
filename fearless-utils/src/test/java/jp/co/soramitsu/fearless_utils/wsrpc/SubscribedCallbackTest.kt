package jp.co.soramitsu.fearless_utils.wsrpc

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.any
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine
import jp.co.soramitsu.fearless_utils.wsrpc.subscription.response.SubscriptionChange
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SubscribedCallbackTest {

    val gson = Gson()

    @Mock
    lateinit var listener: SocketService.ResponseListener<SubscriptionChange>

    @Test
    fun `subscribed callback should handle integer ids`() {
        runIdParseTest(2417711256031439, expected = "2417711256031439")
    }

    @Test
    fun `subscribed callback should handle double ids`() {
        runIdParseTest(2417711256031439.0, expected = "2417711256031439")
    }

    @Test
    fun `subscribed callback should handle string ids`() {
        runIdParseTest("n6in3VIm96u3ABQE", "n6in3VIm96u3ABQE")
    }

    private fun runIdParseTest(id: Any, expected: String) {
        var savedSubscription: SocketStateMachine.Subscription? = null

        val callback = SocketService.SubscribedCallback(
            initiatorId = 123,
            unsubscribeMethod = "test",
            subscriptionCallback = listener,
            gson = gson,
            onSubscribed = {
                savedSubscription = it.subscription
            }
        )

        callback.onNext(RpcResponse("2.0", result = id, id = 123, error = null))

        verify(listener, never()).onError(any())
        assertEquals(expected, savedSubscription?.id)
    }
}