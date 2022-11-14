package jp.co.soramitsu.fearless_utils.wsrpc.socket

import com.google.gson.Gson
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketException
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketFrame
import com.neovisionaries.ws.client.WebSocketState
import jp.co.soramitsu.fearless_utils.util.fromJson
import jp.co.soramitsu.fearless_utils.wsrpc.logging.Logger
import jp.co.soramitsu.fearless_utils.wsrpc.request.base.RpcRequest
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse
import jp.co.soramitsu.fearless_utils.wsrpc.subscription.response.SubscriptionChange
import java.util.concurrent.TimeUnit

interface RpcSocketListener {

    fun onSingleResponse(rpcResponse: RpcResponse)

    fun onSubscriptionResponse(subscriptionChange: SubscriptionChange)

    fun onBatchResponse(batchResponse: List<RpcResponse>)

    fun onStateChanged(newState: WebSocketState)

    fun onConnected()
}

private const val PING_INTERVAL_SECONDS = 30L

class RpcSocket(
    private val url: String,
    listener: RpcSocketListener,
    private val logger: Logger? = null,
    factory: WebSocketFactory,
    private val gson: Gson
) {
    val ws = factory.createSocket(url)

    init {
        setupListener(listener)

        ws.pingInterval = TimeUnit.SECONDS.toMillis(PING_INTERVAL_SECONDS)
    }

    fun connectAsync() {
        log("Connecting", url)

        ws.connectAsynchronously()
    }

    fun clearListeners() {
        ws.clearListeners()
    }

    fun disconnect() {
        ws.disconnect()

        log("Disconnected", url)
    }

    fun sendRpcRequest(rpcRequest: RpcRequest) {
        val text = rpcRequest.toJson()

        log("Sending", text)

        ws.sendText(text)
    }

    fun sendBatchRpcRequests(batchRequests: List<RpcRequest>) {
        // wrap in json array
        val toSend = batchRequests.joinToString(separator = ",", prefix = "[", postfix = "]") {
            it.toJson()
        }

        log("Sending batch", toSend)

        ws.sendText(toSend)
    }

    private fun RpcRequest.toJson(): String {
        return when (this) {
            is RpcRequest.Raw -> content
            is RpcRequest.Rpc2 -> gson.toJson(request)
        }
    }

    private fun setupListener(listener: RpcSocketListener) {
        ws.addListener(object : WebSocketAdapter() {
            override fun onTextMessage(websocket: WebSocket, text: String) {
                log("Received", text)

                when {
                    isBatchReply(text) -> listener.onBatchResponse(gson.fromJson(text))
                    isSubscriptionChange(text) -> listener.onSubscriptionResponse(gson.fromJson(text))
                    else -> listener.onSingleResponse(gson.fromJson(text))
                }
            }

            override fun onPongFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
                log("Received", "Pong")
            }

            override fun onError(websocket: WebSocket, cause: WebSocketException) {
                log("Error", cause.message)
            }

            override fun onConnectError(websocket: WebSocket, exception: WebSocketException) {
                log("Failed to connect", exception.message)
            }

            override fun onStateChanged(websocket: WebSocket, newState: WebSocketState) {
                log("State", newState)

                listener.onStateChanged(newState)
            }

            override fun onConnected(
                websocket: WebSocket?,
                headers: MutableMap<String, MutableList<String>>?
            ) {
                log("Connected", url)

                listener.onConnected()
            }
        })
    }

    private fun log(topic: String, message: Any?) {
        logger?.log("\t[SOCKET][${topic.toUpperCase()}] $message")
    }

    private fun isBatchReply(response: String): Boolean {
        return response.startsWith("[")
    }

    private fun isSubscriptionChange(string: String): Boolean {
        return string.contains("\"subscription\":")
    }
}
