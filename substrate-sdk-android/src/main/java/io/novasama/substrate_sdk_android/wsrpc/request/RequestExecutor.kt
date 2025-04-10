package io.novasama.substrate_sdk_android.wsrpc.request

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

typealias SendAction = () -> Unit

class RequestExecutor(private val executor: ExecutorService = Executors.newSingleThreadExecutor()) {
    private val futures = mutableListOf<Future<*>>()
    private val lock = Any()

    fun execute(action: SendAction) {
        var future: Future<*>? = null

        future = executor.submit {
            action()

            synchronized(lock) {
                futures.remove(future)
            }
        }

        synchronized(lock) {
            futures += future
        }
    }

    fun reset() {
        synchronized(lock) {
            futures.forEach { it.cancel(true) }

            futures.clear()
        }
    }
}
