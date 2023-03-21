package io.novasama.substrate_sdk_android.wsrpc.request.runtime.storage

import io.novasama.substrate_sdk_android.wsrpc.SocketService
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.RuntimeRequest
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.UnsubscribeMethodResolver
import io.novasama.substrate_sdk_android.wsrpc.subscription.response.SubscriptionChange

typealias MultiplexerCallback = SocketService.ResponseListener<StorageSubscriptionMultiplexer.Change>

class StorageSubscriptionMultiplexer(
    private val callbacks: Map<String, List<SocketService.ResponseListener<Change>>>
) : SocketService.ResponseListener<SubscriptionChange> {

    class Change(val block: String, val key: String, val value: String?)

    /**
     * @return subscription request. Null if there were nothing to subscribe for
     */
    fun createRequest(): RuntimeRequest? {
        return if (callbacks.isNotEmpty()) {
            SubscribeStorageRequest(callbacks.keys.toList())
        } else {
            null
        }
    }

    override fun onNext(response: SubscriptionChange) {
        val storageChange = response.storageChange()

        storageChange.changes.forEach { (key, changeValue) ->
            val change = Change(storageChange.block, key!!, changeValue)

            val keyCallbacks = callbacks[key]

            keyCallbacks?.forEach { it.onNext(change) }
        }
    }

    override fun onError(throwable: Throwable) {
        callbacks.values.flatten().onEach { it.onError(throwable) }
    }

    class Builder {
        private val callbacks = mutableMapOf<String, MutableList<MultiplexerCallback>>()

        fun subscribe(key: String, callback: MultiplexerCallback): Builder {
            val currentList = callbacks.getOrPut(key) { mutableListOf() }

            currentList.add(callback)

            return this
        }

        fun build() = StorageSubscriptionMultiplexer(callbacks)
    }
}

fun SocketService.subscribeUsing(multiplexer: StorageSubscriptionMultiplexer): SocketService.Cancellable? {
    val request = multiplexer.createRequest()

    return request?.let {
        subscribe(request, multiplexer, UnsubscribeMethodResolver.resolve(request.method))
    }
}
