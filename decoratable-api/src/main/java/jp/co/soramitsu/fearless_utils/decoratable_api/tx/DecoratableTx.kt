package jp.co.soramitsu.fearless_utils.decoratable_api.tx

import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.metadata.call
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Module
import jp.co.soramitsu.fearless_utils.decoratable_api.Decoratable
import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.runtime.metadata.moduleOrNull
import kotlin.reflect.KType

interface DecoratableTx {

    fun <R : DecoratableFunctions> decorate(moduleName: String, creator: DecoratableFunctions.() -> R): R?
}

internal class DecoratableTxImpl(
    private val api: SubstrateApi,
    private val runtime: RuntimeSnapshot,
) : Decoratable(), DecoratableTx {

    override fun <R : DecoratableFunctions> decorate(moduleName: String, creator: DecoratableFunctions.() -> R): R? = decorateInternal(moduleName) {
        runtime.metadata.moduleOrNull(moduleName)?.let {
            creator(DecoratableFunctionsImpl(api, it))
        }
    }

    private class DecoratableFunctionsImpl(
        private val api: SubstrateApi,
        private val module: Module,
    ) : DecoratableFunctions {

        override val decorator: DecoratableFunctions.Decorator = object : DecoratableFunctions.Decorator, Decoratable() {

            override fun function0(name: String): Function0 {
                return decorateInternal(name) {
                    Function0(module, functionMetadata(name), api)
                }
            }

            override fun <A1> function1(name: String, a1Type: KType): Function1<A1> {
                return decorateInternal(name) {
                    Function1(module, functionMetadata(name), api, a1Type)
                }
            }

            override fun <A1, A2> function2(name: String, a1Type: KType, a2Type: KType): Function2<A1, A2> {
                return decorateInternal(name) {
                    Function2(module, functionMetadata(name), api, a1Type, a2Type)
                }
            }

            override fun <A1, A2, A3> function3(name: String, a1Type: KType, a2Type: KType, a3Type: KType): Function3<A1, A2, A3> {
                return decorateInternal(name) {
                    Function3(module, functionMetadata(name), api, a1Type, a2Type, a3Type)
                }
            }

            private fun functionMetadata(name: String) = module.call(name)
        }
    }
}
