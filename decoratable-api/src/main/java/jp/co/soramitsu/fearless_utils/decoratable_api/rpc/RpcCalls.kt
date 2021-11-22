package jp.co.soramitsu.fearless_utils.decoratable_api.rpc

import jp.co.soramitsu.fearless_utils.coroutines_adapter.executeAsync
import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.RuntimeRequest
import jp.co.soramitsu.fearless_utils.wsrpc.response.resultOrThrow

class RpcCall0<R>(
    moduleName: String,
    callName: String,
    socketService: SocketService,
    rpcBindingContext: RpcBindingContext,
    binder: RpcCallBinding<R>,
) : RpcCallBase<R>(
    moduleName,
    callName,
    socketService,
    rpcBindingContext,
    binder
) {

    suspend operator fun invoke(): R {
        return performCall(emptyList())
    }
}

class RpcCallList<A, R>(
    moduleName: String,
    callName: String,
    socketService: SocketService,
    rpcBindingContext: RpcBindingContext,
    binder: RpcCallBinding<R>,
) : RpcCallBase<R>(
    moduleName,
    callName,
    socketService,
    rpcBindingContext,
    binder
) {

    suspend operator fun invoke(vararg arguments: A): R {
        return performCall(arguments.toList())
    }

    suspend operator fun invoke(arguments: List<A>): R {
        return performCall(arguments)
    }
}

class RpcCall1<A, R>(
    moduleName: String,
    callName: String,
    socketService: SocketService,
    rpcBindingContext: RpcBindingContext,
    binder: RpcCallBinding<R>,
) : RpcCallBase<R>(
    moduleName,
    callName,
    socketService,
    rpcBindingContext,
    binder
) {

    suspend operator fun invoke(argument: A): R {
        return performCall(listOf(argument))
    }
}


abstract class RpcCallBase<R>(
    private val moduleName: String,
    private val callName: String,
    private val socketService: SocketService,
    private val rpcBindingContext: RpcBindingContext,
    private val binder: RpcCallBinding<R>,
) {

    protected suspend fun performCall(params: List<Any?>): R {
        val method = "${moduleName}_${callName}"

        val request = RuntimeRequest(method, params)
        val rpcResponse = socketService.executeAsync(request)

        return binder(rpcBindingContext, rpcResponse.resultOrThrow())
    }
}

