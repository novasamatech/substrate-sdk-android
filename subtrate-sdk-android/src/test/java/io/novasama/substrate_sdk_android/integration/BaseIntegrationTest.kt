package io.novasama.substrate_sdk_android.integration

import com.google.gson.Gson
import com.neovisionaries.ws.client.WebSocketFactory
import io.novasama.substrate_sdk_android.wsrpc.SocketService
import io.novasama.substrate_sdk_android.wsrpc.StdoutLogger
import io.novasama.substrate_sdk_android.wsrpc.recovery.ConstantReconnectStrategy
import io.novasama.substrate_sdk_android.wsrpc.recovery.Reconnector
import io.novasama.substrate_sdk_android.wsrpc.request.RequestExecutor
import org.junit.After
import org.junit.Before

abstract class BaseIntegrationTest(private val networkUrl: String = KUSAMA_URL) {

    protected val socketService = SocketService(
        Gson(),
        StdoutLogger,
        WebSocketFactory(),
        Reconnector(strategy = ConstantReconnectStrategy(1000L)),
        RequestExecutor()
    )

    @Before
    open fun setup() {
        socketService.start(networkUrl)
    }

    @After
    open fun tearDown() {
        socketService.stop()
    }
}
