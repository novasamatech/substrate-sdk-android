package io.novasama.substrate_sdk_android.wsrpc.request.runtime.storage

import io.novasama.substrate_sdk_android.wsrpc.SocketService
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.RuntimeRequest
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.UnsubscribeMethodResolver
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.storage.StorageSubscriptionMultiplexer.*
import io.novasama.substrate_sdk_android.wsrpc.subscription.response.SubscriptionChange

typealias MultiplexerCallback = SocketService.ResponseListener<Change>
typealias MultiplexerBatchCallback = SocketService.ResponseListener<BatchChange>

class StorageSubscriptionMultiplexer(
    private val singleKeyCallbacks: Map<String, List<SocketService.ResponseListener<Change>>>,
    private val multiKeyCallbacks: List<MultiKeyCallbackRecord>
) : SocketService.ResponseListener<SubscriptionChange> {

    class Change(val block: String, val key: String, val value: String?)

    class BatchChange(val block: String, val changedKeys: List<Pair<String, String?>>)

    /**
     * @return subscription request. Null if there were nothing to subscribe for
     */
    fun createRequest(): RuntimeRequest? {
        val allUniqueKeys =
            singleKeyCallbacks.keys + multiKeyCallbacks.flatMapTo(mutableSetOf()) { it.keys }

        return if (allUniqueKeys.isNotEmpty()) {
            SubscribeStorageRequest(allUniqueKeys.toList())
        } else {
            null
        }
    }

    override fun onNext(response: SubscriptionChange) {
        val storageChange = response.storageChange()

        notifyIndividualChanges(storageChange)
        notifyBatchChanges(storageChange)
    }

    override fun onError(throwable: Throwable) {
        singleKeyCallbacks.values.flatten().onEach { it.onError(throwable) }
        multiKeyCallbacks.forEach { it.callback.onError(throwable) }
    }

    private fun notifyIndividualChanges(storageResult: SubscribeStorageResult) {
        if (singleKeyCallbacks.isEmpty()) return

        storageResult.changes.forEach { (key, changeValue) ->
            val change = Change(storageResult.block, key!!, changeValue)

            val keyCallbacks = singleKeyCallbacks[key]

            keyCallbacks?.forEach { it.onNext(change) }
        }
    }

    // Notify multi key callbacks who are interested in some subset of changed keys
    private fun notifyBatchChanges(storageResult: SubscribeStorageResult) {
        if (multiKeyCallbacks.isEmpty()) return

        val allChangedKeys = storageResult.changes.mapTo(mutableSetOf()) { it.first() as String }

        multiKeyCallbacks.forEach { multiKeyCallbackRecord ->
            val relatedModifiedKeys = multiKeyCallbackRecord.keys.intersect(allChangedKeys)

            if (relatedModifiedKeys.isNotEmpty()) {
                val changedPairs = storageResult.changes.mapNotNull { (key, value) ->
                    if (key !in relatedModifiedKeys) {
                        return@mapNotNull null
                    }

                    key as String to value
                }

                val change = BatchChange(storageResult.block, changedPairs)
                multiKeyCallbackRecord.callback.onNext(change)
            }
        }
    }

    class Builder {
        private val singleKeyCallbacks = mutableMapOf<String, MutableList<MultiplexerCallback>>()
        private val multiKeyCallbacks = mutableListOf<MultiKeyCallbackRecord>()

        fun subscribe(key: String, callback: MultiplexerCallback): Builder {
            val currentList = singleKeyCallbacks.getOrPut(key) { mutableListOf() }

            currentList.add(callback)

            return this
        }

        /**
         * Subscribe to a set of keys. [callback] will be called when any of the passed keys change
         * The [BatchChange.changedKeys] will contain intersection between subscription changes and passed [keys]
         */
        fun subscribe(keys: Iterable<String>, callback: MultiplexerBatchCallback): Builder {
            multiKeyCallbacks.add(MultiKeyCallbackRecord(keys.toSet(), callback))
            return this
        }

        fun build() = StorageSubscriptionMultiplexer(singleKeyCallbacks, multiKeyCallbacks)
    }

    class MultiKeyCallbackRecord(
        val keys: Set<String>,
        val callback: MultiplexerBatchCallback
    )
}

fun SocketService.subscribeUsing(multiplexer: StorageSubscriptionMultiplexer): SocketService.Cancellable? {
    val request = multiplexer.createRequest()

    return request?.let {
        subscribe(request, multiplexer, UnsubscribeMethodResolver.resolve(request.method))
    }
}
