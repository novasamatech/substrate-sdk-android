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

            override fun <R> plain(name: String, binder: AnyBinding<R>): PlainStorageEntry<R> {
                return decorateInternal(name) {
                    PlainStorageEntry(runtime, storageEntryMetadata(name), api, bindingContext, binder)
                }
            }

            override fun <K, R> map1(name: String, binder: AnyBinding<R>): SingleMapStorageEntry<K, R> {
                return decorateInternal(name) {
                    SingleMapStorageEntry(runtime, storageEntryMetadata(name), api, bindingContext, binder)
                }
            }

            private fun storageEntryMetadata(name: String): StorageEntry = module.storage(name)
        }
    }
}
