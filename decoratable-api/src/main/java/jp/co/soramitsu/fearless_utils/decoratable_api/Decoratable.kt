package jp.co.soramitsu.fearless_utils.decoratable_api

abstract class Decoratable {
    private val initialized = mutableMapOf<String, Any?>()

    @Suppress("UNCHECKED_CAST")
    protected fun <R> decorateInternal(key: String, lazyCreate: () -> R): R {
        return initialized.getOrPut(key) {
            lazyCreate()
        } as R
    }
}
