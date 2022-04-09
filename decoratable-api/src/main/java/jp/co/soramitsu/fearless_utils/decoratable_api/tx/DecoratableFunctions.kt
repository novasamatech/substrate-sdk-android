package jp.co.soramitsu.fearless_utils.decoratable_api.tx

import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface DecoratableFunctions {

    val decorator: Decorator

    interface Decorator {

        fun function0(name: String): Function0

        fun <A1> function1(name: String, a1Type: KType): Function1<A1>

        fun <A1, A2> function2(name: String, a1Type: KType, a2Type: KType): Function2<A1, A2>

        fun <A1, A2, A3> function3(name: String, a1Type: KType, a2Type: KType, a3Type: KType): Function3<A1, A2, A3>
    }
}

// for unified imports during codegen
@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "NOTHING_TO_INLINE")
inline fun DecoratableFunctions.Decorator.function0(name: String) = function0(name)

inline fun <reified A1> DecoratableFunctions.Decorator.function1(name: String) = function1<A1>(name, typeOf<A1>())

inline fun <reified A1, reified A2> DecoratableFunctions.Decorator.function2(name: String) = function2<A1, A2>(name, typeOf<A1>(), typeOf<A2>())

inline fun <reified A1, reified A2, reified A3> DecoratableFunctions.Decorator.function3(name: String): Function3<A1, A2, A3> {
    return function3(name, typeOf<A1>(), typeOf<A2>(), typeOf<A3>())
}
