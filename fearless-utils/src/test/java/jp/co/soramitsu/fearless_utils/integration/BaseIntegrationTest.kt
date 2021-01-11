package jp.co.soramitsu.fearless_utils.integration

import com.google.gson.Gson
import com.neovisionaries.ws.client.WebSocketFactory
import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import jp.co.soramitsu.fearless_utils.wsrpc.StdoutLogger
import org.junit.After
import org.junit.Before

abstract class BaseIntegrationTest(private val networkUrl: String = KUSAMA_URL) {
    protected val socketService = SocketService(Gson(), StdoutLogger, WebSocketFactory())

    @Before
    fun setup() {
        socketService.start(networkUrl)
    }

    @After
    fun tearDown() {
        socketService.stop()
    }
}