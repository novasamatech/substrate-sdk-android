package jp.co.soramitsu.fearless_utils.decoratable_api.query

interface DecoratableStorage {

    val decorator: Decorator

    interface Decorator {
        fun <R> plain(name: String, binder: (Any?) -> R): PlainStorageEntry<R>

        fun <K, R> single(name: String, binder: (Any?) -> R): SingleMapStorageEntry<K, R>
    }
}
