package io.novasama.substrate_sdk_android.wsrpc.request.runtime

import io.novasama.substrate_sdk_android.wsrpc.subscription.response.SubscriptionChange

internal fun createFakeChange(
    result: Any,
    subscriptionId: String = "test"
): SubscriptionChange {
    return SubscriptionChange(
        jsonrpc = "test",
        method = "test",
        params = SubscriptionChange.Params(
            result = result,
            subscription = subscriptionId
        )
    )
}