package jp.co.soramitsu.fearless_utils.decoratable_api.query

import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.AnyBinding
import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.Bindings

interface DecoratableStorage {

    val decorator: Decorator

    interface Decorator {
        fun <R> plain(name: String, binder: AnyBinding<R>): PlainStorageEntry<R>

        fun <K, R> map1(name: String, binder: AnyBinding<R>): SingleMapStorageEntry<K, R>
    }
}

inline fun <reified R> DecoratableStorage.Decorator.plain(name: String): PlainStorageEntry<R> = plain(name, Bindings.dynamicBinder())
inline fun <reified R, K> DecoratableStorage.Decorator.map1(name: String): SingleMapStorageEntry<K, R> = map1(name, Bindings.dynamicBinder())
