package jp.co.soramitsu.fearless_utils.wsrpc.request

import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import jp.co.soramitsu.fearless_utils.wsrpc.request.base.RpcRequest
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine

internal class RespondableSendable(
    val request: RpcRequest,
    override val deliveryType: DeliveryType,
    val callback: SocketService.ResponseListener<RpcResponse>
) : SocketStateMachine.Sendable {
    override val id: Int = request.id

    override fun toString(): String {
        return "Sendable($id)"
    }
}
