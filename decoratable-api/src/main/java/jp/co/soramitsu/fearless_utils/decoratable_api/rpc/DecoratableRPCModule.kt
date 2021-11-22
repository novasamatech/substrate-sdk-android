package jp.co.soramitsu.fearless_utils.decoratable_api.rpc

import jp.co.soramitsu.fearless_utils.json.JsonCodec
import jp.co.soramitsu.fearless_utils.json.fromParsedHierarchy
import jp.co.soramitsu.fearless_utils.wsrpc.subscription.response.SubscriptionChange
import java.math.BigInteger

interface RpcBindingContext {

    val jsonCodec: JsonCodec
}

typealias RpcBinding<I, O> = RpcBindingContext.(I) -> O
typealias RpcCallBinding<O> = RpcBinding<Any?, O>
typealias RpcSubscriptionBinding<O> = RpcBinding<SubscriptionChange, O>

interface DecoratableRPCModule {

    val decorator: Decorator

    interface Decorator {

        fun <R> call0(callName: String, binder: RpcCallBinding<R>): RpcCall0<R>

        fun <A, R> call1(callName: String, binder: RpcCallBinding<R>): RpcCall1<A, R>

        fun <A, R> callList(callName: String, binder: RpcCallBinding<R>): RpcCallList<A, R>

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

private val TO_STRING: RpcCallBinding<String> = { it.toString() }
private val TO_OPTIONAL_STRING: RpcCallBinding<String?> = { it?.toString() }

// Some Json parsers may parse integers as Double
private val TO_NUMBER: RpcCallBinding<BigInteger> = {
    when(it) {
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

@Suppress("unused")
inline fun <reified T> DecoratableRPCModule.Decorator.asJson(): RpcCallBinding<T> = {
    jsonCodec.fromParsedHierarchy(it)
}
