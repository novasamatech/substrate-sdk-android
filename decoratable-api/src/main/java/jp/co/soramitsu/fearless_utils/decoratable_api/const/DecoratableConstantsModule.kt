package jp.co.soramitsu.fearless_utils.decoratable_api.const

import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.AnyBinding
import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.Bindings
import java.math.BigInteger

interface DecoratableConstantsModule {

    val decorator: Decorator

    interface Decorator {

        fun <R> constant(name: String, binding: AnyBinding<R>): Constant<R>?
    }
}

inline fun <reified R> DecoratableConstantsModule.Decorator.constant(name: String): Constant<R>? {
    return constant(name, Bindings.dynamicBinder())
}
