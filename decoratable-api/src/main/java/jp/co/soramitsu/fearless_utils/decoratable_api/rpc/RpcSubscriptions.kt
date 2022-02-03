package jp.co.soramitsu.fearless_utils.decoratable_api.rpc

import jp.co.soramitsu.fearless_utils.coroutines_adapter.subscriptionFlow
import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.RuntimeRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RpcSubscription0<R>(
    moduleName: String,
    callName: String,
    socketService: SocketService,
    rpcBindingContext: RpcBindingContext,
    binder: RpcSubscriptionBinding<R>,
) : RpcSubscriptionBase<R>(
    moduleName,
    callName,
    socketService,
    rpcBindingContext,
    binder
) {

    operator fun invoke(): Flow<R> {
        return subscribe(emptyList())
    }
}

class RpcSubscriptionList<A, R>(
    moduleName: String,
    callName: String,
    socketService: SocketService,
    rpcBindingContext: RpcBindingContext,
    binder: RpcSubscriptionBinding<R>,
) : RpcSubscriptionBase<R>(
    moduleName,
    callName,
    socketService,
    rpcBindingContext,
    binder
) {

    operator fun invoke(vararg arguments: A): Flow<R> {
        return subscribe(arguments.toList())
    }

    operator fun invoke(arguments: List<A>): Flow<R> {
        return subscribe(arguments)
    }
}

class RpcSubscription1<A, R>(
    moduleName: String,
    callName: String,
    socketService: SocketService,
    rpcBindingContext: RpcBindingContext,
    binder: RpcSubscriptionBinding<R>,
) : RpcSubscriptionBase<R>(
    moduleName,
    callName,
    socketService,
    rpcBindingContext,
    binder
) {

    operator fun invoke(argument: A): Flow<R> {
        return subscribe(listOf(argument))
    }
}

abstract class RpcSubscriptionBase<R>(
    private val moduleName: String,
    private val callName: String,
    private val socketService: SocketService,
    private val rpcBindingContext: RpcBindingContext,
    private val binder: RpcSubscriptionBinding<R>,
) {

    protected fun subscribe(params: List<Any?>): Flow<R> {
        val method = "${moduleName}_$callName"

        val request = RuntimeRequest(method, params)

        // TODO add handling for rpc errors
        return socketService.subscriptionFlow(request)
            .map { binder(rpcBindingContext, it) }
    }
}
