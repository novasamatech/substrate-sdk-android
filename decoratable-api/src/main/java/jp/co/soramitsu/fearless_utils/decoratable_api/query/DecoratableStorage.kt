package jp.co.soramitsu.fearless_utils.decoratable_api.query

import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.AnyBinding
import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.Bindings
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface DecoratableStorage {

    val decorator: Decorator

    interface Decorator {
        fun <R> map0(name: String, binder: AnyBinding<R>): StorageEntry0<R>

        fun <K, R> map1(name: String, a1Type: KType, binder: AnyBinding<R>): StorageEntry1<K, R>

        fun <K1, K2, R> map2(name: String,  a1Type: KType, a2Type: KType, binder: AnyBinding<R>): StorageEntry2<K1, K2, R>

        fun <K1, K2, K3, R> map3(name: String,  a1Type: KType, a2Type: KType, a3Type: KType, binder: AnyBinding<R>): StorageEntry3<K1, K2, K3, R>
    }
}

inline fun <reified R> DecoratableStorage.Decorator.map0(name: String): StorageEntry0<R> {
    return map0(name, Bindings.dynamicBinder())
}

inline fun <reified R, reified K> DecoratableStorage.Decorator.map1(name: String): StorageEntry1<K, R> {
    return map1(name, typeOf<K>(), Bindings.dynamicBinder())
}

inline fun <reified R, reified K1, reified K2> DecoratableStorage.Decorator.map2(name: String): StorageEntry2<K1, K2, R> {
    return map2(name, typeOf<K1>(), typeOf<K2>(), Bindings.dynamicBinder())
}

inline fun <reified R, reified K1, reified K2, reified K3> DecoratableStorage.Decorator.map3(name: String): StorageEntry3<K1, K2, K3, R> {
    return map3(name, typeOf<K1>(), typeOf<K2>(), typeOf<K3>(), Bindings.dynamicBinder())
}
