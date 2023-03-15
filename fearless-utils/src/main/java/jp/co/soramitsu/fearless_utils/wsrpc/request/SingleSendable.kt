package jp.co.soramitsu.fearless_utils.wsrpc.request

import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import jp.co.soramitsu.fearless_utils.wsrpc.request.base.RpcRequest
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse
import jp.co.soramitsu.fearless_utils.wsrpc.socket.RpcSocket
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine

internal class SingleSendable(
    val request: RpcRequest,
    override val deliveryType: DeliveryType,
    override val callback: SocketService.ResponseListener<RpcResponse>
) : SocketStateMachine.Sendable {
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
