package jp.co.soramitsu.fearless_utils.decoratable_api.query

import jp.co.soramitsu.fearless_utils.decoratable_api.Decoratable
import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.AnyBinding
import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.BindingContext
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.metadata.module
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Module
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.StorageEntry
import jp.co.soramitsu.fearless_utils.runtime.metadata.storage
import kotlin.reflect.KType

class DecoratableQuery(
    private val api: SubstrateApi,
    private val bindingContext: BindingContext,
    private val runtime: RuntimeSnapshot,
) : Decoratable() {

    fun <R : DecoratableStorage> decorate(moduleName: String, creator: DecoratableStorage.() -> R): R = decorateInternal(moduleName) {
        val module = runtime.metadata.module(moduleName)

        creator(DecoratableStorageImpl(api, runtime, bindingContext, module))
    }

    private class DecoratableStorageImpl(
        private val api: SubstrateApi,
        private val runtime: RuntimeSnapshot,
        private val bindingContext: BindingContext,
        private val module: Module,
    ) : DecoratableStorage {

        override val decorator: DecoratableStorage.Decorator = object : DecoratableStorage.Decorator, Decoratable() {

            override fun <R> map0(name: String, binder: AnyBinding<R>): StorageEntry0<R> {
                return decorateInternal(name) {
                    StorageEntry0(runtime, storageEntryMetadata(name), api, bindingContext, binder)
                }
            }

            override fun <K, R> map1(name: String, a1Type: KType, binder: AnyBinding<R>): StorageEntry1<K, R> {
                return decorateInternal(name) {
                    StorageEntry1(runtime, storageEntryMetadata(name), api, bindingContext, binder, a1Type)
                }
            }

            override fun <K1, K2, R> map2(name: String, a1Type: KType, a2Type: KType, binder: AnyBinding<R>): StorageEntry2<K1, K2, R> {
                return decorateInternal(name) {
                    StorageEntry2(runtime, storageEntryMetadata(name), api, bindingContext, binder, a1Type, a2Type)
                }
            }

            override fun <K1, K2, K3, R> map3(name: String, a1Type: KType, a2Type: KType, a3Type: KType, binder: AnyBinding<R>): StorageEntry3<K1, K2, K3, R> {
                return decorateInternal(name) {
                    StorageEntry3(runtime, storageEntryMetadata(name), api, bindingContext, binder, a1Type, a2Type, a3Type)
                }
            }

            private fun storageEntryMetadata(name: String): StorageEntry = module.storage(name)
        }
    }
}
