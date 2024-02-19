package io.novasama.substrate_sdk_android.wsrpc.request.base

import io.novasama.substrate_sdk_android.wsrpc.request.runtime.RuntimeRequest

sealed class RpcRequest(val id: Int) {

    class Raw(val content: String, id: Int) : RpcRequest(id)

    class Rpc2(val request: RuntimeRequest) : RpcRequest(request.id)
}
