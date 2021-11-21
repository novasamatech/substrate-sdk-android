package jp.co.soramitsu.fearless_utils.wsrpc.logging

class NoOpLogger : Logger {

    override fun log(message: String?) {
        // pass
    }

    override fun log(throwable: Throwable?) {
       // pass
    }
}