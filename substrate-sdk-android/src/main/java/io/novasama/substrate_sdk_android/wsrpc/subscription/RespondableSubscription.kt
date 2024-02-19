package io.novasama.substrate_sdk_android.wsrpc.subscription

import io.novasama.substrate_sdk_android.wsrpc.SocketService.ResponseListener
import io.novasama.substrate_sdk_android.wsrpc.state.SocketStateMachine
import io.novasama.substrate_sdk_android.wsrpc.subscription.response.SubscriptionChange

class RespondableSubscription(
    override val id: String,
    override val initiatorId: Int,
    val unsubscribeMethod: String,
    val callback: ResponseListener<SubscriptionChange>
) : SocketStateMachine.Subscription {

    override fun toString(): String {
        return "Subscription(id=$id, initiatorId=$initiatorId)"
    }
}
