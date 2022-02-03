package jp.co.soramitsu.fearless_utils.decoratable_api.const

import jp.co.soramitsu.fearless_utils.decoratable_api.Decoratable
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.metadata.constant
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Module
import jp.co.soramitsu.fearless_utils.runtime.metadata.moduleOrNull

internal class DecoratableConstImpl(
    private val runtime: RuntimeSnapshot
) : Decoratable(), DecoratableConst {

    override fun <R : DecoratableConstantsModule> decorate(
        moduleName: String,
        creator: DecoratableConstantsModule.() -> R
    ): R? = decorateInternal(moduleName) {
        runtime.metadata.moduleOrNull(moduleName)?.let {
            creator(DecoratableConstantsModuleImpl(runtime, it))
        }
    }

    private class DecoratableConstantsModuleImpl(
        private val runtime: RuntimeSnapshot,
        private val module: Module
    ) : DecoratableConstantsModule {

        override val decorator: DecoratableConstantsModule.Decorator =
            object : DecoratableConstantsModule.Decorator, Decoratable() {

                override fun <R> constant(name: String, binding: ConstantsBinding<R>): Constant<R> {
                    return decorateInternal(name) {
                        Constant(runtime, module.constant(name), binding)
                    }
                }
            }
    }
}
