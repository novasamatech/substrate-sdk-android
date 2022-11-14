package jp.co.soramitsu.fearless_utils.wsrpc.request

import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import jp.co.soramitsu.fearless_utils.wsrpc.request.base.RpcRequest
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine

internal class SingleSendable(
    val request: RpcRequest,
    override val deliveryType: DeliveryType,
    val callback: SocketService.ResponseListener<RpcResponse>
) : SocketStateMachine.Sendable {
    override val id: Int = request.id

    override fun toString(): String {
        return "Sendable($id)"
    }
}

internal class BatchSendable(
    val requests: List<RpcRequest>,
    override val deliveryType: DeliveryType,
    val callback: SocketService.ResponseListener<List<RpcResponse>>
) : SocketStateMachine.Sendable {

    override val id: Int = requests.first().id

    override fun toString(): String {
        val jointIds = requests.joinToString { it.id.toString() }

        return "BatchSendable($jointIds)"
    }
}
