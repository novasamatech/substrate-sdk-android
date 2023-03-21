package io.novasama.substrate_sdk_android.wsrpc

import io.novasama.substrate_sdk_android.wsrpc.logging.Logger

object StdoutLogger : Logger {
    override fun log(message: String?) {
        println(message)
    }

    override fun log(throwable: Throwable?) {
        println(throwable)
    }
}