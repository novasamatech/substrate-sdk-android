package jp.co.soramitsu.fearless_utils.decoratable_api.rpc

import jp.co.soramitsu.fearless_utils.wsrpc.subscription.response.SubscriptionChange
import jp.co.soramitsu.feature_wallet_impl.data.buyToken.rpc.RpcCall0
import jp.co.soramitsu.feature_wallet_impl.data.buyToken.rpc.RpcCall1
import jp.co.soramitsu.feature_wallet_impl.data.buyToken.rpc.RpcCallList
import jp.co.soramitsu.feature_wallet_impl.data.buyToken.rpc.RpcSubscription0
import jp.co.soramitsu.feature_wallet_impl.data.buyToken.rpc.RpcSubscription1
import jp.co.soramitsu.feature_wallet_impl.data.buyToken.rpc.RpcSubscriptionList

interface DecoratableRPCModule {

    val decorator: Decorator

    interface Decorator {

        fun <R> call0(callName: String, binder: (Any?) -> R): RpcCall0<R>

        fun <A : Any, R> call1(callName: String, binder: (Any?) -> R): RpcCall1<A, R>

        fun <A : Any, R> callList(callName: String, binder: (Any?) -> R): RpcCallList<A, R>

        fun <R> subscription0(callName: String, binder: (SubscriptionChange) -> R): RpcSubscription0<R>

        fun <A : Any, R> subscription1(callName: String, binder: (SubscriptionChange) -> R): RpcSubscription1<A, R>

        fun <A : Any, R> subscriptionList(callName: String, binder: (SubscriptionChange) -> R): RpcSubscriptionList<A, R>
    }
}

private val TO_STRING: (Any?) -> String = { it.toString() }
private val TO_OPTIONAL_STRING: (Any?) -> String? = { it?.toString() }

@Suppress("unused")
val DecoratableRPCModule.Decorator.asString
    get() = TO_STRING

@Suppress("unused")
val DecoratableRPCModule.Decorator.asOptionalString
    get() = TO_OPTIONAL_STRING
