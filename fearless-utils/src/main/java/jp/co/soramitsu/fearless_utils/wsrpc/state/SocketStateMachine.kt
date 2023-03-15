package jp.co.soramitsu.fearless_utils.wsrpc.state

import jp.co.soramitsu.fearless_utils.wsrpc.SocketService
import jp.co.soramitsu.fearless_utils.wsrpc.request.DeliveryType
import jp.co.soramitsu.fearless_utils.wsrpc.response.RpcResponse
import jp.co.soramitsu.fearless_utils.wsrpc.socket.RpcSocket
import jp.co.soramitsu.fearless_utils.wsrpc.subscription.response.SubscriptionChange

typealias Transition = Pair<SocketStateMachine.State, List<SocketStateMachine.SideEffect>>

private typealias ResponseCounter = Int

object SocketStateMachine {

    interface Sendable {

        val numberOfNeededResponses: Int

        fun relatesTo(id: Int): Boolean

        val deliveryType: DeliveryType

        val callback: SocketService.ResponseListener<RpcResponse>

        fun sendTo(rpcSocket: RpcSocket)
    }

    interface Subscription {

        val id: String

        val initiatorId: Int
    }

    object ConnectionClosedException : Exception("Connection was closed")

    sealed class State {
        data class WaitingForReconnect(
            val url: String,
            val attempt: Int = 0,
            internal val pendingSendables: Set<Sendable>
        ) : State()

        data class Connecting(
            val url: String,
            val attempt: Int = 0,
            internal val pendingSendables: Set<Sendable> = emptySet()
        ) :
            State()

        data class Connected(
            val url: String,
            internal val toResendOnReconnect: Set<Sendable>,
            internal val unknownSubscriptionResponses: Map<String, SubscriptionChange>,
            internal val waitingForResponse: Map<Sendable, ResponseCounter>,
            internal val subscriptions: Set<Subscription>
        ) : State()

        object Disconnected : State()

        data class Paused(
            val url: String,
            internal val pendingSendables: Set<Sendable> = emptySet()
        ) : State()

        override fun toString(): String = javaClass.simpleName
    }

    sealed class Event {
        data class Send(val sendable: Sendable) : Event()

        data class Cancel(val sendable: Sendable) : Event()

        data class SendableResponse(val response: RpcResponse) : Event()

        data class SendableBatchResponse(val responses: List<RpcResponse>) : Event()

        data class Subscribed(val subscription: Subscription) : Event()

        data class SubscriptionResponse(val response: SubscriptionChange) : Event()

        data class ConnectionError(val throwable: Throwable) : Event()

        object Connected : Event()

        object ReadyToReconnect : Event()

        object Stop : Event()

        class Start(val url: String, val remainPaused: Boolean) : Event()

        class SwitchUrl(val url: String) : Event()

        object Pause : Event()

        object Resume : Event()

        override fun toString(): String = javaClass.simpleName
    }

    sealed class SideEffect {

        data class ResponseToSendable(val sendable: Sendable, val response: RpcResponse) :
            SideEffect()

        /**
         * For [DeliveryType.AT_MOST_ONCE] errors
         */
        data class RespondSendablesError(
            val sendables: Set<Sendable>,
            val error: Throwable
        ) : SideEffect()

        data class RespondToSubscription(
            val subscription: Subscription,
            val change: SubscriptionChange
        ) : SideEffect()

        data class Unsubscribe(val subscription: Subscription) : SideEffect()

        data class SendSendables(val sendables: Set<Sendable>) : SideEffect()

        data class Connect(val url: String) : SideEffect()

        data class ScheduleReconnect(val attempt: Int) : SideEffect()

        object Disconnect : SideEffect()

        override fun toString(): String = javaClass.simpleName
    }

    fun initialState(): State = State.Disconnected

    fun transition(
        state: State,
        event: Event
    ): Transition {
        val sideEffects = mutableListOf<SideEffect>()

        val newState = when (state) {
            is State.WaitingForReconnect -> {
                when (event) {
                    is Event.ReadyToReconnect -> {
                        sideEffects += SideEffect.Connect(state.url)
                        State.Connecting(state.url, state.attempt, state.pendingSendables)
                    }
                    is Event.Send -> {
                        sideEffects += SideEffect.Connect(state.url)
                        State.Connecting(
                            url = state.url,
                            attempt = 0,
                            pendingSendables = state.pendingSendables + event.sendable
                        )
                    }
                    is Event.Cancel -> {
                        state.copy(pendingSendables = state.pendingSendables - event.sendable)
                    }
                    is Event.Stop -> handleStop(sideEffects)
                    is Event.SwitchUrl -> {
                        applySwitchUrlSideEffects(event.url, sideEffects)

                        State.Connecting(event.url, attempt = 0, state.pendingSendables)
                    }
                    is Event.Pause -> {
                        applyPauseEffects(sideEffects)

                        State.Paused(url = state.url, pendingSendables = state.pendingSendables)
                    }
                    else -> state
                }
            }

            is State.Connecting -> {
                when (event) {
                    is Event.Send -> {
                        state.copy(pendingSendables = state.pendingSendables + event.sendable)
                    }
                    is Event.Cancel -> {
                        state.copy(pendingSendables = state.pendingSendables - event.sendable)
                    }
                    is Event.ConnectionError -> {
                        val newAttempt = state.attempt + 1

                        sideEffects += SideEffect.ScheduleReconnect(newAttempt)
                        State.WaitingForReconnect(state.url, newAttempt, state.pendingSendables)
                    }
                    is Event.Connected -> {
                        if (state.pendingSendables.isNotEmpty()) {
                            sideEffects += SideEffect.SendSendables(state.pendingSendables)
                        }

                        State.Connected(
                            url = state.url,
                            toResendOnReconnect = state.pendingSendables.filterByDeliveryType(
                                DeliveryType.ON_RECONNECT
                            ),
                            waitingForResponse = state.pendingSendables.withCounter(),
                            subscriptions = emptySet(),
                            unknownSubscriptionResponses = emptyMap()
                        )
                    }
                    is Event.Stop -> handleStop(sideEffects)
                    is Event.SwitchUrl -> {
                        applySwitchUrlSideEffects(event.url, sideEffects)

                        State.Connecting(event.url, attempt = 0, state.pendingSendables)
                    }
                    is Event.Pause -> {
                        applyPauseEffects(sideEffects)

                        State.Paused(url = state.url, pendingSendables = state.pendingSendables)
                    }
                    else -> state
                }
            }

            is State.Connected -> {
                when (event) {
                    is Event.Send -> {
                        val sendable = event.sendable

                        sideEffects += SideEffect.SendSendables(setOf(sendable))

                        val toResendOnReconnect =
                            if (sendable.deliveryType == DeliveryType.ON_RECONNECT) {
                                state.toResendOnReconnect + sendable
                            } else {
                                state.toResendOnReconnect
                            }

                        state.copy(
                            toResendOnReconnect = toResendOnReconnect,
                            waitingForResponse = state.waitingForResponse.add(sendable)
                        )
                    }

                    is Event.Cancel -> {
                        val sendable = event.sendable

                        val subscription =
                            findSubscriptionByInitiator(state.subscriptions, sendable)

                        val subscriptions = subscription?.let {
                            sideEffects += SideEffect.Unsubscribe(it)

                            state.subscriptions - it
                        } ?: state.subscriptions

                        state.copy(
                            toResendOnReconnect = state.toResendOnReconnect - sendable,
                            waitingForResponse = state.waitingForResponse - sendable,
                            subscriptions = subscriptions
                        )
                    }

                    is Event.SendableResponse -> {
                        val entry = findSendableById(state.waitingForResponse, event.response.id)

                        if (entry != null) {
                            val (sendable, counter) = entry
                            sideEffects += SideEffect.ResponseToSendable(sendable, event.response)

                            val newCounter = counter + 1

                            state.copy(
                                waitingForResponse = state.waitingForResponse.keepUntilThreshold(
                                    key = sendable,
                                    newValue = newCounter,
                                    threshold = sendable.numberOfNeededResponses
                                )
                            )
                        } else {
                            state
                        }
                    }

                    is Event.SendableBatchResponse -> {
                        val newWaitingForResponse = state.waitingForResponse.toMutableMap()

                        event.responses.forEach { response ->
                            val entry = findSendableById(state.waitingForResponse, response.id)

                            if (entry != null) {
                                val (sendable, counter) = entry
                                val newCounter = counter + 1

                                sideEffects += SideEffect.ResponseToSendable(
                                    sendable = sendable,
                                    response = response
                                )

                                newWaitingForResponse.keepUntilThreshold(
                                    key = sendable,
                                    newValue = newCounter,
                                    threshold = sendable.numberOfNeededResponses
                                )
                            }
                        }

                        state.copy(waitingForResponse = newWaitingForResponse)
                    }

                    is Event.Subscribed -> {
                        val subscriptionId = event.subscription.id

                        state.unknownSubscriptionResponses[subscriptionId]?.let {
                            sideEffects += SideEffect.RespondToSubscription(
                                subscription = event.subscription,
                                change = it
                            )
                        }

                        val newUnknown = state.unknownSubscriptionResponses - subscriptionId

                        state.copy(
                            subscriptions = state.subscriptions + event.subscription,
                            unknownSubscriptionResponses = newUnknown
                        )
                    }

                    is Event.SubscriptionResponse -> {
                        val subscription = findSubscriptionById(
                            state.subscriptions,
                            event.response.subscriptionId
                        )

                        if (subscription != null) {
                            sideEffects += SideEffect.RespondToSubscription(
                                subscription,
                                event.response
                            )

                            state
                        } else {
                            val mapEntry = event.response.subscriptionId to event.response
                            val newUnknown = state.unknownSubscriptionResponses + mapEntry

                            state.copy(
                                unknownSubscriptionResponses = newUnknown
                            )
                        }
                    }

                    is Event.ConnectionError -> {
                        val toResend = getRequestsToResendAndReportErrorToOthers(
                            state, sideEffects, event.throwable
                        )

                        sideEffects += SideEffect.ScheduleReconnect(attempt = 0)
                        State.WaitingForReconnect(url = state.url, pendingSendables = toResend)
                    }

                    is Event.Stop -> handleStop(sideEffects)
                    is Event.SwitchUrl -> {
                        val toResend = getRequestsToResendAndReportErrorToOthers(
                            state, sideEffects, ConnectionClosedException
                        )

                        applySwitchUrlSideEffects(event.url, sideEffects)

                        State.Connecting(url = event.url, attempt = 0, pendingSendables = toResend)
                    }
                    is Event.Pause -> {
                        val toResend = getRequestsToResendAndReportErrorToOthers(
                            state, sideEffects, ConnectionClosedException
                        )

                        applyPauseEffects(sideEffects)

                        State.Paused(url = state.url, pendingSendables = toResend)
                    }
                    else -> state
                }
            }

            is State.Disconnected -> {
                when (event) {
                    is Event.Start -> {
                        if (event.remainPaused) {
                            State.Paused(url = event.url)
                        } else {
                            sideEffects += SideEffect.Connect(event.url)

                            State.Connecting(event.url)
                        }
                    }
                    else -> state
                }
            }

            is State.Paused -> {
                when (event) {
                    is Event.Send -> state.copy(
                        pendingSendables = state.pendingSendables + event.sendable
                    )

                    is Event.Cancel -> state.copy(
                        pendingSendables = state.pendingSendables - event.sendable
                    )

                    is Event.Stop -> State.Disconnected // do not emit Disconnect Side Effect since connection is already stopped

                    is Event.SwitchUrl -> State.Paused(event.url, state.pendingSendables)

                    is Event.Resume -> {
                        sideEffects += SideEffect.Connect(state.url)

                        State.Connecting(url = state.url, pendingSendables = state.pendingSendables)
                    }

                    else -> state
                }
            }
        }

        return Transition(newState, sideEffects)
    }

    private fun MutableMap<Sendable, ResponseCounter>.keepUntilThreshold(
        key: Sendable,
        newValue: ResponseCounter,
        threshold: ResponseCounter
    ) {
        if (newValue >= threshold) {
            minusAssign(key)
        } else {
            plusAssign(key to newValue)
        }
    }

    // we keep separate method for read-only `minus` since it optimizes result internally
    private fun Map<Sendable, ResponseCounter>.keepUntilThreshold(
        key: Sendable,
        newValue: ResponseCounter,
        threshold: ResponseCounter
    ): Map<Sendable, ResponseCounter> {
        return if (newValue >= threshold) {
            minus(key)
        } else {
            plus(key to newValue)
        }
    }


    private fun Map<Sendable, ResponseCounter>.add(sendable: Sendable) = plus(sendable to 0)

    private fun Set<Sendable>.withCounter() = associateWith { 0 }

    private fun getRequestsToResendAndReportErrorToOthers(
        state: State.Connected,
        mutableSideEffects: MutableList<SideEffect>,
        error: Throwable
    ): Set<Sendable> {
        val waitingSendables = state.waitingForResponse.keys

        val toReportError = waitingSendables.filterByDeliveryType(DeliveryType.AT_MOST_ONCE)

        if (toReportError.isNotEmpty()) {
            mutableSideEffects += SideEffect.RespondSendablesError(
                toReportError,
                error
            )
        }

        return waitingSendables - toReportError + state.toResendOnReconnect
    }

    private fun findSubscriptionById(subscriptions: Set<Subscription>, id: String) =
        subscriptions.find { it.id == id }

    private fun findSendableById(sendables: Map<Sendable, ResponseCounter>, id: Int) =
        sendables.entries.find { (sendable, _) -> sendable.relatesTo(id) }

    private fun findSubscriptionByInitiator(subscriptions: Set<Subscription>, initiator: Sendable) =
        subscriptions.find { initiator.relatesTo(it.initiatorId) }

    private fun handleStop(sideEffects: MutableList<SideEffect>): State {
        sideEffects += SideEffect.Disconnect

        return State.Disconnected
    }

    private fun applySwitchUrlSideEffects(url: String, sideEffects: MutableList<SideEffect>) {
        sideEffects += listOf(SideEffect.Disconnect, SideEffect.Connect(url))
    }

    private fun applyPauseEffects(sideEffects: MutableList<SideEffect>) {
        sideEffects += SideEffect.Disconnect
    }

    private fun Set<Sendable>.filterByDeliveryType(deliveryType: DeliveryType): Set<Sendable> =
        filterTo(mutableSetOf()) {
            it.deliveryType == deliveryType
        }
}
