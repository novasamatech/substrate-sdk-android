package jp.co.soramitsu.fearless_utils.decoratable_api.tx

import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface DecoratableFunctions {

    val decorator: Decorator

    interface Decorator {

        fun function0(name: String): Function0

        fun <A1> function1(name: String, a1Type: KType): Function1<A1>

        fun <A1, A2> function2(name: String, a1Type: KType, a2Type: KType): Function2<A1, A2>
    }
}

inline fun <reified A1, reified A2> DecoratableFunctions.Decorator.function2(name: String) = function2<A1, A2>(name, typeOf<A1>(), typeOf<A2>())
inline fun <reified A1> DecoratableFunctions.Decorator.function1(name: String) = function1<A1>(name, typeOf<A1>())
