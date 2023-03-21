package io.novasama.substrate_sdk_android.wsrpc.logging

interface Logger {
    fun log(message: String?)

    fun log(throwable: Throwable?)
}
