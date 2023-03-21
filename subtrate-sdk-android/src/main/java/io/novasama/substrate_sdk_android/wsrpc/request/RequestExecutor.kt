package io.novasama.substrate_sdk_android.wsrpc.request

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

typealias SendAction = () -> Unit

class RequestExecutor(private val executor: ExecutorService = Executors.newSingleThreadExecutor()) {
    private val futures = mutableListOf<Future<*>>()

    fun execute(action: SendAction) {
        var future: Future<*>? = null

        future = executor.submit {
            action()

            futures.remove(future)
        }

        futures += future
    }

    fun reset() {
        futures.forEach { it.cancel(true) }

        futures.clear()
    }
}
