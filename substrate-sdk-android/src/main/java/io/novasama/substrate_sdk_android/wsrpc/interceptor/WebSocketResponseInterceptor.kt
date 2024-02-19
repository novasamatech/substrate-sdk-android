package io.novasama.substrate_sdk_android.wsrpc.interceptor

import io.novasama.substrate_sdk_android.wsrpc.response.RpcResponse

interface WebSocketResponseInterceptor {

    enum class ResponseDelivery {
        DROP, DELIVER_TO_SENDER
    }

    fun onRpcResponseReceived(rpcResponse: RpcResponse): ResponseDelivery
}

fun WebSocketResponseInterceptor.ResponseDelivery.shouldDeliver(): Boolean {
    return this == WebSocketResponseInterceptor.ResponseDelivery.DELIVER_TO_SENDER
}
