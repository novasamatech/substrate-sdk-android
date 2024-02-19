@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.novasama.substrate_sdk_android.wsrpc

import io.novasama.substrate_sdk_android.wsrpc.mappers.ResponseMapper
import io.novasama.substrate_sdk_android.wsrpc.request.DeliveryType
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.RuntimeRequest
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.UnsubscribeMethodResolver
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.storage.MultiplexerCallback
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.storage.StorageSubscriptionMultiplexer.Builder
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.storage.StorageSubscriptionMultiplexer.Change
import io.novasama.substrate_sdk_android.wsrpc.response.RpcResponse
import io.novasama.substrate_sdk_android.wsrpc.socket.StateObserver
import io.novasama.substrate_sdk_android.wsrpc.state.SocketStateMachine.State
import io.novasama.substrate_sdk_android.wsrpc.subscription.response.SubscriptionChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <R> SocketService.executeAsync(
    request: RuntimeRequest,
    deliveryType: DeliveryType = DeliveryType.AT_LEAST_ONCE,
    mapper: ResponseMapper<R>
): R {
    val response = executeAsync(request, deliveryType)

    return withContext(Dispatchers.Default) {
        mapper.map(response, jsonMapper)
    }
}

suspend fun SocketService.executeAsync(
    request: RuntimeRequest,
    deliveryType: DeliveryType = DeliveryType.AT_LEAST_ONCE
) = suspendCancellableCoroutine<RpcResponse> { cont ->
    val cancellable =
        executeRequest(
            request, deliveryType,
            object : SocketService.ResponseListener<RpcResponse> {
                override fun onNext(response: RpcResponse) {
                    cont.resume(response)
                }

                override fun onError(throwable: Throwable) {
                    cont.resumeWithException(throwable)
                }
            }
        )

    cont.invokeOnCancellation {
        cancellable.cancel()
    }
}

fun SocketService.subscriptionFlow(
    request: RuntimeRequest,
    unsubscribeMethod: String = UnsubscribeMethodResolver.resolve(request.method)
): Flow<SubscriptionChange> =
    callbackFlow {
        val cancellable =
            subscribe(
                request,
                object : SocketService.ResponseListener<SubscriptionChange> {
                    override fun onNext(response: SubscriptionChange) {
                        offer(response)
                    }

                    override fun onError(throwable: Throwable) {
                        close(throwable)
                    }
                },
                unsubscribeMethod
            )

        awaitClose {
            cancellable.cancel()
        }
    }

fun SocketService.networkStateFlow(): Flow<State> = callbackFlow {
    val observer: StateObserver = { state: State ->
        offer(state)
    }

    addStateObserver(observer)

    awaitClose {
        removeStateObserver(observer)
    }
}

fun Builder.subscribe(key: String): Flow<Change> {
    val callback = FlowCallback()

    subscribe(key, callback)

    return callback.collector
        .map { it.getOrThrow() }
}

private class FlowCallback : MultiplexerCallback {

    val collector = MutableSharedFlow<Result<Change>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun onNext(response: Change) {
        collector.tryEmit(Result.success(response))
    }

    override fun onError(throwable: Throwable) {
        collector.tryEmit(Result.failure(throwable))
    }
}
