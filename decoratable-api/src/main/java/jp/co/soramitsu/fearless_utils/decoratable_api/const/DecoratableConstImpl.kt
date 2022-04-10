package jp.co.soramitsu.fearless_utils.decoratable_api.const

import jp.co.soramitsu.fearless_utils.decoratable_api.Decoratable
import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.AnyBinding
import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.BindingContext
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.metadata.constant
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Module
import jp.co.soramitsu.fearless_utils.runtime.metadata.moduleOrNull

internal class DecoratableConstImpl(
    private val bindingContext: BindingContext,
    private val runtime: RuntimeSnapshot
) : Decoratable(), DecoratableConst {

    override fun <R : DecoratableConstantsModule> decorate(
        moduleName: String,
        creator: DecoratableConstantsModule.() -> R
    ): R? = decorateInternal(moduleName) {
        runtime.metadata.moduleOrNull(moduleName)?.let {
            creator(DecoratableConstantsModuleImpl(bindingContext, runtime, it))
        }
    }

    private class DecoratableConstantsModuleImpl(
        private val bindingContext: BindingContext,
        private val runtime: RuntimeSnapshot,
        private val module: Module
    ) : DecoratableConstantsModule {

        override val decorator: DecoratableConstantsModule.Decorator =
            object : DecoratableConstantsModule.Decorator, Decoratable() {

                override fun <R> constant(name: String, binding: AnyBinding<R>): Constant<R> {
                    return decorateInternal(name) {
                        Constant(runtime, module.constant(name), bindingContext, binding)
                    }
                }
            }
    }
}
