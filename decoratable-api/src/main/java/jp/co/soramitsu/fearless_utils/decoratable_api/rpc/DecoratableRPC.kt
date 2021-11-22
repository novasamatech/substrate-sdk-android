package jp.co.soramitsu.fearless_utils.decoratable_api.rpc

import jp.co.soramitsu.fearless_utils.json.JsonCodec
import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import jp.co.soramitsu.fearless_utils.decoratable_api.Decoratable

class DecoratableRPC(
    override val jsonCodec: JsonCodec,
    private val socketService: SocketService,
) : Decoratable(), RpcBindingContext {

    fun <R : DecoratableRPCModule> decorate(
        moduleName: String,
        creator: DecoratableRPCModule.() -> R
    ): R = decorateInternal(moduleName) {
        creator(DecoratableRPCModuleImpl(this, moduleName, socketService))
    }

    private class DecoratableRPCModuleImpl(
        private val rpcBindingContext: RpcBindingContext,
        private val moduleName: String,
        private val socketService: SocketService,
    ) : DecoratableRPCModule {

        override val decorator: DecoratableRPCModule.Decorator =
            object : DecoratableRPCModule.Decorator, Decoratable() {

                override fun <R> call0(callName: String, binder: RpcCallBinding<R>): RpcCall0<R> {
                    return decorateInternal(callName) {
                        RpcCall0(moduleName, callName, socketService, rpcBindingContext, binder)
                    }
                }

                override fun <A, R> call1(
                    callName: String,
                    binder: RpcCallBinding<R>
                ): RpcCall1<A, R> {
                    return decorateInternal(callName) {
                        RpcCall1(moduleName, callName, socketService, rpcBindingContext, binder)
                    }
                }

                override fun <A, R> callList(
                    callName: String,
                    binder: RpcCallBinding<R>
                ): RpcCallList<A, R> {
                    return decorateInternal(callName) {
                        RpcCallList(
                            moduleName = moduleName,
                            callName = callName,
                            socketService = socketService,
                            rpcBindingContext,
                            binder
                        )
                    }
                }

                override fun <R> subscription0(
                    callName: String,
                    binder: RpcSubscriptionBinding<R>
                ): RpcSubscription0<R> {
                    return decorateInternal(callName) {
                        RpcSubscription0(
                            moduleName = moduleName,
                            callName = callName,
                            socketService = socketService,
                            rpcBindingContext = rpcBindingContext,
                            binder = binder
                        )
                    }
                }

                override fun <A, R> subscription1(
                    callName: String,
                    binder: RpcSubscriptionBinding<R>
                ): RpcSubscription1<A, R> {
                    return decorateInternal(callName) {
                        RpcSubscription1(
                            moduleName = moduleName,
                            callName = callName,
                            socketService = socketService,
                            rpcBindingContext = rpcBindingContext,
                            binder = binder
                        )
                    }
                }

                override fun <A, R> subscriptionList(
                    callName: String,
                    binder: RpcSubscriptionBinding<R>
                ): RpcSubscriptionList<A, R> {
                    return decorateInternal(callName) {
                        RpcSubscriptionList(
                            moduleName = moduleName,
                            callName = callName,
                            socketService = socketService,
                            rpcBindingContext = rpcBindingContext,
                            binder = binder
                        )
                    }
                }
            }
    }
}
