package jp.co.soramitsu.fearless_utils.wsrpc.recovery

import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

private val DEFAULT_RECONNECT_STRATEGY =
    ExponentialReconnectStrategy(initialTime = 300L, base = 2.0)

class Reconnector(
    private val strategy: ReconnectStrategy = DEFAULT_RECONNECT_STRATEGY,
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
) {
    private var inProgress: Future<*>? = null

    fun scheduleConnect(currentAttempt: Int, runnable: Runnable) {
        reset()

        inProgress = executor.schedule(
            wrapReconnectAction(runnable),
            strategy.getTimeForReconnect(currentAttempt),
            TimeUnit.MILLISECONDS
        )
    }

    fun reset() {
        inProgress?.cancel(true)
        inProgress = null
    }

    private fun wrapReconnectAction(how: Runnable) = Runnable {
        inProgress = null

        how.run()
    }
}
