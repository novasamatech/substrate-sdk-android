package jp.co.soramitsu.fearless_utils.decoratable_api.rpc

import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.AnyBinding
import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.Binding
import jp.co.soramitsu.fearless_utils.wsrpc.subscription.response.SubscriptionChange
import java.math.BigInteger

typealias RpcSubscriptionBinding<O> = Binding<SubscriptionChange, O>

interface DecoratableRPCModule {

    val decorator: Decorator

    interface Decorator {

        fun <R> call0(callName: String, binder: AnyBinding<R>): RpcCall0<R>

        fun <A, R> call1(callName: String, binder: AnyBinding<R>): RpcCall1<A, R>

        fun <A, R> callList(callName: String, binder: AnyBinding<R>): RpcCallList<A, R>

        fun <R> subscription0(
            callName: String,
            binder: RpcSubscriptionBinding<R>
        ): RpcSubscription0<R>

        fun <A, R> subscription1(
            callName: String,
            binder: RpcSubscriptionBinding<R>
        ): RpcSubscription1<A, R>

        fun <A, R> subscriptionList(
            callName: String,
            binder: RpcSubscriptionBinding<R>
        ): RpcSubscriptionList<A, R>
    }
}

private val TO_STRING: AnyBinding<String> = { it.toString() }
private val TO_OPTIONAL_STRING: AnyBinding<String?> = { it?.toString() }

// Some Json parsers may parse integers as Double
private val TO_NUMBER: AnyBinding<BigInteger> = {
    when (it) {
        is Double -> it.toInt().toBigInteger()
        is Int -> it.toBigInteger()
        else -> it.toString().toBigInteger()
    }
}

@Suppress("unused")
val DecoratableRPCModule.Decorator.asString
    get() = TO_STRING

@Suppress("unused")
val DecoratableRPCModule.Decorator.asOptionalString
    get() = TO_OPTIONAL_STRING

@Suppress("unused")
val DecoratableRPCModule.Decorator.asNumber
    get() = TO_NUMBER
