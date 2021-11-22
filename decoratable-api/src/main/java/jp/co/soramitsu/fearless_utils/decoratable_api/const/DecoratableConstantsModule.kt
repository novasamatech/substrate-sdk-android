package jp.co.soramitsu.fearless_utils.decoratable_api.const

import java.math.BigInteger

typealias ConstantsBinding<O> = (String) -> O

interface DecoratableConstantsModule {

    val decorator: Decorator

    interface Decorator {

        fun <R> constant(name: String, binding: ConstantsBinding<R>): Constant<R>
    }
}

@Suppress("unused")
fun DecoratableConstantsModule.Decorator.number(): ConstantsBinding<BigInteger> = {
    it.toBigInteger()
}

fun DecoratableConstantsModule.Decorator.numberConstant(name: String): Constant<BigInteger> {
    return constant(name, number())
}