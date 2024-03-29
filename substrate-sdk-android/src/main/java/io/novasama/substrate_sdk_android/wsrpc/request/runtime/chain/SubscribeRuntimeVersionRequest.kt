package io.novasama.substrate_sdk_android.wsrpc.request.runtime.chain

import io.novasama.substrate_sdk_android.wsrpc.request.runtime.RuntimeRequest
import io.novasama.substrate_sdk_android.wsrpc.subscription.response.SubscriptionChange
import io.novasama.substrate_sdk_android.wsrpc.subscription.response.notValidResult

object SubscribeRuntimeVersionRequest : RuntimeRequest(
    method = "chain_subscribeRuntimeVersion",
    params = listOf()
)

fun SubscriptionChange.runtimeVersionChange(): RuntimeVersion {
    val result = params.result as? Map<*, *> ?: notValidResult(params.result)

    val specVersion = result["specVersion"] as? Double ?: notValidResult(result)
    val transactionVersion = result["transactionVersion"] as? Double ?: notValidResult(result)

    return RuntimeVersion(specVersion.toInt(), transactionVersion.toInt())
}

private fun notValidResult(result: Any?): Nothing = notValidResult(result, "runtime version")
