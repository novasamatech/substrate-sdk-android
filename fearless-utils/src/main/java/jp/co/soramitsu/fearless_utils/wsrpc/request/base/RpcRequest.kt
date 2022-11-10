package jp.co.soramitsu.fearless_utils.wsrpc.request.base

import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.RuntimeRequest

sealed class RpcRequest(val id: Int) {

    class Raw(val content: String, id: Int) : RpcRequest(id)

    class Rpc2(val request: RuntimeRequest) : RpcRequest(request.id)
}
