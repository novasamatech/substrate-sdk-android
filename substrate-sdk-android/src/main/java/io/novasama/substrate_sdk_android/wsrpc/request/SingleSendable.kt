package io.novasama.substrate_sdk_android.wsrpc.request

import io.novasama.substrate_sdk_android.wsrpc.SocketService
import io.novasama.substrate_sdk_android.wsrpc.request.base.RpcRequest
import io.novasama.substrate_sdk_android.wsrpc.response.RpcResponse
import io.novasama.substrate_sdk_android.wsrpc.socket.RpcSocket
import io.novasama.substrate_sdk_android.wsrpc.state.SocketStateMachine

internal class SingleSendable(
    val request: RpcRequest,
    override val deliveryType: DeliveryType,
    override val callback: SocketService.ResponseListener<RpcResponse>
) : SocketStateMachine.Sendable {

    override val numberOfNeededResponses: Int = 1

    override fun relatesTo(id: Int): Boolean = request.id == id

    override fun sendTo(rpcSocket: RpcSocket) {
        rpcSocket.sendRpcRequest(request)
    }

    override fun toString(): String {
        return "Sendable(${request.id})"
    }
}

internal class BatchSendable(
    val requests: List<RpcRequest>,
    override val deliveryType: DeliveryType,
    override val callback: SocketService.ResponseListener<RpcResponse>
) : SocketStateMachine.Sendable {

    override val numberOfNeededResponses: Int = requests.size

    private val ids = requests.mapTo(mutableSetOf(), RpcRequest::id)

    override fun relatesTo(id: Int): Boolean = id in ids

    override fun sendTo(rpcSocket: RpcSocket) {
        rpcSocket.sendBatchRpcRequests(requests)
    }

    override fun toString(): String {
        val jointIds = requests.joinToString { it.id.toString() }

        return "BatchSendable($jointIds)"
    }
}
