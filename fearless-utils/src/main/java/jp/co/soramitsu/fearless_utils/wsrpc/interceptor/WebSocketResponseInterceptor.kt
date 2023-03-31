package jp.co.soramitsu.fearless_utils.wsrpc.interceptor

import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse


interface WebSocketResponseInterceptor {

    enum class ResponseDelivery {
        DROP, DELIVER_TO_SENDER
    }

    fun onRpcResponseReceived(rpcResponse: RpcResponse): ResponseDelivery
}

fun WebSocketResponseInterceptor.ResponseDelivery.shouldDeliver(): Boolean {
    return this == WebSocketResponseInterceptor.ResponseDelivery.DELIVER_TO_SENDER
}