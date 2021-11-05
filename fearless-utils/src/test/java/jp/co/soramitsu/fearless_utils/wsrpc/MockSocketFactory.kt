package jp.co.soramitsu.fearless_utils.wsrpc

import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketFactory
import org.mockito.Mockito

class MockSocketFactory: WebSocketFactory() {

    val mockSocket = Mockito.mock(WebSocket::class.java)

    override fun createSocket(uri: String?): WebSocket {
        return mockSocket
    }
}