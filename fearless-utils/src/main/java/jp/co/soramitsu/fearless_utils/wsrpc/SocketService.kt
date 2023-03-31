package jp.co.soramitsu.fearless_utils.wsrpc

import com.google.gson.Gson
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketState
import jp.co.soramitsu.fearless_utils.wsrpc.exception.ConnectionClosedException
import jp.co.soramitsu.fearless_utils.wsrpc.interceptor.WebSocketResponseInterceptor
import jp.co.soramitsu.fearless_utils.wsrpc.interceptor.shouldDeliver
import jp.co.soramitsu.fearless_utils.wsrpc.logging.Logger
import jp.co.soramitsu.fearless_utils.wsrpc.mappers.nonNull
import jp.co.soramitsu.fearless_utils.wsrpc.mappers.stringIdMapper
import jp.co.soramitsu.fearless_utils.wsrpc.recovery.Reconnector
import jp.co.soramitsu.fearless_utils.wsrpc.request.BatchSendable
import jp.co.soramitsu.fearless_utils.wsrpc.request.DeliveryType
import jp.co.soramitsu.fearless_utils.wsrpc.request.RequestExecutor
import jp.co.soramitsu.fearless_utils.wsrpc.request.SingleSendable
import jp.co.soramitsu.fearless_utils.wsrpc.request.base.RpcRequest
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.RuntimeRequest
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse
import jp.co.soramitsu.fearless_utils.wsrpc.socket.ObservableState
import jp.co.soramitsu.fearless_utils.wsrpc.socket.RpcSocket
import jp.co.soramitsu.fearless_utils.wsrpc.socket.RpcSocketListener
import jp.co.soramitsu.fearless_utils.wsrpc.socket.StateObserver
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.Event
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.SideEffect
import jp.co.soramitsu.fearless_utils.wsrpc.state.SocketStateMachine.State
import jp.co.soramitsu.fearless_utils.wsrpc.subscription.RespondableSubscription
import jp.co.soramitsu.fearless_utils.wsrpc.subscription.response.SubscriptionChange

class SocketService(
    val jsonMapper: Gson,
    private val logger: Logger,
    private val webSocketFactory: WebSocketFactory,
    private val reconnector: Reconnector,
    private val requestExecutor: RequestExecutor,
    private val interceptor: WebSocketResponseInterceptor? = null,
) : RpcSocketListener {

    private var socket: RpcSocket? = null

    private val stateContainer = ObservableState(initialState = State.Disconnected)

    fun started() = stateContainer.getState() !is State.Disconnected

    /**
     * Initiate new connection to the given url.
     * Only has effect when called from [SocketStateMachine.State.Disconnected] state.
     * Meaning, this is the first method that should be called after create [SocketService] instance
     *
     * @param url - url to connect to
     * @param remainPaused - true if socket should start in [SocketStateMachine.State.Paused] state,
     * delaying connection until [resume] is called
     */
    fun start(url: String, remainPaused: Boolean = false) {
        updateState(Event.Start(url, remainPaused))
    }

    /**
     * Closes connection and forgets about all requests and subscriptions.
     * Should be used when socket connection is no longer needed.
     */
    fun stop() {
        updateState(Event.Stop)
    }

    /**
     * Seamlessly switches connection url saving all pending & active requests or subscriptions
     * Should be used instead of
     * ```
     *     stop()
     *     start(newURl)
     * ```
     */
    fun switchUrl(url: String) {
        updateState(Event.SwitchUrl(url))
    }

    /**
     * Closes connection but saves all subscriptions and [DeliveryType.AT_LEAST_ONCE] requests.
     * Reports [ConnectionClosedException] to [DeliveryType.AT_MOST_ONCE] requests.
     */
    fun pause() {
        updateState(Event.Pause)
    }

    /**
     * Resumes connection from [SocketStateMachine.State.Paused] state,
     * recovering all subscriptions and [DeliveryType.AT_LEAST_ONCE] requests
     */
    fun resume() {
        updateState(Event.Resume)
    }

    fun addStateObserver(observer: StateObserver) = stateContainer.addObserver(observer)

    fun removeStateObserver(observer: StateObserver) = stateContainer.removeObserver(observer)

    @Synchronized
    fun subscribe(
        request: RuntimeRequest,
        callback: ResponseListener<SubscriptionChange>,
        unsubscribeMethod: String
    ): Cancellable {
        return subscribe(RpcRequest.Rpc2(request), callback, unsubscribeMethod)
    }

    @Synchronized
    fun subscribe(
        request: RpcRequest,
        callback: ResponseListener<SubscriptionChange>,
        unsubscribeMethod: String
    ): Cancellable {
        return executeRequest(
            request,
            DeliveryType.ON_RECONNECT,
            SubscribedCallback(
                initiatorId = request.id,
                unsubscribeMethod = unsubscribeMethod,
                subscriptionCallback = callback,
                gson = jsonMapper,
                onSubscribed = ::updateState
            )
        )
    }

    @Synchronized
    fun executeRequest(
        rpcRequest: RpcRequest,
        deliveryType: DeliveryType = DeliveryType.AT_LEAST_ONCE,
        callback: ResponseListener<RpcResponse>
    ): Cancellable {
        val sendable = SingleSendable(rpcRequest, deliveryType, callback)

        updateState(Event.Send(sendable))

        return RequestCancellable(sendable)
    }

    @Synchronized
    fun executeBatchRequest(
        rpcRequests: List<RpcRequest>,
        deliveryType: DeliveryType = DeliveryType.AT_LEAST_ONCE,
        callback: ResponseListener<RpcResponse>
    ): Cancellable {
        val sendable = BatchSendable(rpcRequests, deliveryType, callback)

        updateState(Event.Send(sendable))

        return RequestCancellable(sendable)
    }

    @Synchronized
    fun executeAccumulatingBatchRequest(
        rpcRequests: List<RpcRequest>,
        deliveryType: DeliveryType = DeliveryType.AT_LEAST_ONCE,
        allBatchDoneCallback: ResponseListener<List<RpcResponse>>
    ): Cancellable {
        val callback = AccumulatingBatchRequest(allBatchDoneCallback, rpcRequests.size)
        val sendable = BatchSendable(rpcRequests, deliveryType, callback)

        updateState(Event.Send(sendable))

        return RequestCancellable(sendable)
    }

    @Synchronized
    fun executeRequest(
        runtimeRequest: RuntimeRequest,
        deliveryType: DeliveryType = DeliveryType.AT_LEAST_ONCE,
        callback: ResponseListener<RpcResponse>
    ): Cancellable {
        return executeRequest(
            rpcRequest = RpcRequest.Rpc2(runtimeRequest),
            deliveryType = deliveryType,
            callback = callback
        )
    }

    @Synchronized
    override fun onSingleResponse(rpcResponse: RpcResponse) {
        if (
            interceptor != null &&
            interceptor.onRpcResponseReceived(rpcResponse).shouldDeliver()
        ) {
            updateState(Event.SendableResponse(rpcResponse))
        }
    }

    override fun onSubscriptionResponse(subscriptionChange: SubscriptionChange) {
        updateState(Event.SubscriptionResponse(subscriptionChange))
    }

    @Synchronized
    override fun onBatchResponse(batchResponse: List<RpcResponse>) {
        val toDeliver = if (interceptor != null) {
            batchResponse.filter { interceptor.onRpcResponseReceived(it).shouldDeliver() }
        } else {
            batchResponse
        }

        if (toDeliver.isNotEmpty()) {
            updateState(Event.SendableBatchResponse(batchResponse))
        }
    }

    @Synchronized
    override fun onConnected() {
        updateState(Event.Connected)
    }

    @Synchronized
    override fun onStateChanged(newState: WebSocketState) {
        if (newState == WebSocketState.CLOSED) {
            updateState(Event.ConnectionError(ConnectionClosedException()))
        }
    }

    @Synchronized
    private fun updateState(event: Event) {
        val state = stateContainer.getState()
        val (newState, sideEffects) = SocketStateMachine.transition(state, event)
        stateContainer.setState(newState)

        logger.log("[STATE MACHINE][TRANSITION] $event : $state -> $newState")

        sideEffects.forEach(::consumeSideEffect)
    }

    private fun consumeSideEffect(sideEffect: SideEffect) {
        logger.log("[STATE MACHINE][SIDE EFFECT] $sideEffect")

        when (sideEffect) {
            is SideEffect.ResponseToSendable -> respondToRequest(
                sideEffect.sendable,
                sideEffect.response
            )
            is SideEffect.RespondSendablesError -> respondError(
                sideEffect.sendables,
                sideEffect.error
            )
            is SideEffect.RespondToSubscription -> respondToSubscription(
                sideEffect.subscription,
                sideEffect.change
            )
            is SideEffect.SendSendables -> sendToSocket(sideEffect.sendables)
            is SideEffect.Connect -> connectToSocket(sideEffect.url)
            is SideEffect.ScheduleReconnect -> scheduleReconnect(sideEffect.attempt)
            is SideEffect.Disconnect -> disconnect()
            is SideEffect.Unsubscribe -> unsubscribe(sideEffect.subscription)
        }
    }

    private fun respondToRequest(
        sendable: SocketStateMachine.Sendable,
        response: RpcResponse
    ) {
        sendable.callback.onNext(response)
    }

    private fun respondError(sendables: Set<SocketStateMachine.Sendable>, throwable: Throwable) {
        sendables.forEach { sendable -> sendable.callback.onError(throwable) }
    }

    private fun respondToSubscription(
        subscription: SocketStateMachine.Subscription,
        response: SubscriptionChange
    ) {
        require(subscription is RespondableSubscription)

        subscription.callback.onNext(response)
    }

    private fun sendToSocket(sendables: Collection<SocketStateMachine.Sendable>) {
        requestExecutor.execute {
            sendables.forEach { sendable -> sendable.sendTo(socket!!) }
        }
    }

    private fun scheduleReconnect(attempt: Int) =
        reconnector.scheduleConnect(attempt, ::readyForReconnect)

    private fun readyForReconnect() {
        updateState(Event.ReadyToReconnect)
    }

    private fun connectToSocket(url: String) {
        reconnector.reset()

        socket = createSocket(url)
        socket!!.connectAsync()
    }

    private fun createSocket(url: String) =
        RpcSocket(url, this, logger, webSocketFactory, jsonMapper)

    private fun disconnect() {
        socket!!.clearListeners()
        socket!!.disconnect()
        socket = null

        requestExecutor.reset()
        reconnector.reset()
    }

    private fun unsubscribe(subscription: SocketStateMachine.Subscription) {
        require(subscription is RespondableSubscription)

        val unsubscribeRequest =
            RuntimeRequest(subscription.unsubscribeMethod, listOf(subscription.id))

        executeRequest(
            unsubscribeRequest,
            deliveryType = DeliveryType.AT_MOST_ONCE,
            FireAndForgetCallback()
        )
    }

    interface ResponseListener<R> {
        fun onNext(response: R)

        fun onError(throwable: Throwable)
    }

    interface Cancellable {
        fun cancel()
    }

    class SubscribedCallback(
        private val initiatorId: Int,
        private val unsubscribeMethod: String,
        private val subscriptionCallback: ResponseListener<SubscriptionChange>,
        private val gson: Gson,
        private val onSubscribed: (Event.Subscribed) -> Unit
    ) : ResponseListener<RpcResponse> {

        override fun onNext(response: RpcResponse) {
            val id = try {
                stringIdMapper().nonNull().map(response, gson)
            } catch (e: Exception) {
                subscriptionCallback.onError(e)

                return
            }

            val subscription =
                RespondableSubscription(id, initiatorId, unsubscribeMethod, subscriptionCallback)

            onSubscribed(Event.Subscribed(subscription))
        }

        override fun onError(throwable: Throwable) {
            subscriptionCallback.onError(throwable)
        }
    }

    inner class RequestCancellable(
        private val sendable: SocketStateMachine.Sendable
    ) : Cancellable {

        override fun cancel() {
            updateState(Event.Cancel(sendable))
        }
    }

    private class FireAndForgetCallback : ResponseListener<RpcResponse> {

        override fun onNext(response: RpcResponse) {
            // do nothing
        }

        override fun onError(throwable: Throwable) {
            // do nothing
        }
    }

    private class AccumulatingBatchRequest(
        private val allDoneCallBack: ResponseListener<List<RpcResponse>>,
        private val batchSize: Int
    ) : ResponseListener<RpcResponse> {

        private var arrivedResponses: MutableList<RpcResponse> = ArrayList(batchSize)

        override fun onNext(response: RpcResponse) {
            arrivedResponses.add(response)

            if (arrivedResponses.size == batchSize) {
                allDoneCallBack.onNext(arrivedResponses)
            }
        }

        override fun onError(throwable: Throwable) {
            allDoneCallBack.onError(throwable)
        }
    }
}
