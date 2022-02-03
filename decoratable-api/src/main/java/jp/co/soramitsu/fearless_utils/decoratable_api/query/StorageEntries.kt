package jp.co.soramitsu.fearless_utils.decoratable_api.query

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.state.getStorage
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.state.state
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.state.subscribeStorage
import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.AnyBinding
import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.BindingContext
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.StorageEntry
import jp.co.soramitsu.fearless_utils.runtime.metadata.storageKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

abstract class StorageEntryBase<R>(
    protected val runtime: RuntimeSnapshot,
    protected val storageEntryMetadata: StorageEntry,
    private val api: SubstrateApi,
    private val bindingContext: BindingContext,
    val binder: AnyBinding<R>,
) {

    protected suspend fun query(key: String): R? {
        val result: String = api.rpc.state.getStorage(key) ?: return null

        val decoded = storageEntryMetadata.type.value!!.fromHex(runtime, result)

        return binder(bindingContext, decoded)
    }

    protected fun subscribe(key: String): Flow<R?> {
        return api.rpc.state.subscribeStorage(listOf(key))
            .map { changes ->
                val (_, change) = changes.first()

                change?.let {
                    val decoded = storageEntryMetadata.type.value!!.fromHex(runtime, change)

                    binder(bindingContext, decoded)
                }
            }
    }
}

class PlainStorageEntry<R>(
    runtime: RuntimeSnapshot,
    storageEntryMetadata: StorageEntry,
    api: SubstrateApi,
    bindingContext: BindingContext,
    binder: AnyBinding<R>,
) : StorageEntryBase<R>(runtime, storageEntryMetadata, api, bindingContext, binder) {

    suspend operator fun invoke(): R? {
        return query(storageEntryMetadata.storageKey())
    }

    fun subscribe(): Flow<R?> = subscribe(storageEntryMetadata.storageKey())
}

class SingleMapStorageEntry<K, R>(
    runtime: RuntimeSnapshot,
    storageEntryMetadata: StorageEntry,
    api: SubstrateApi,
    bindingContext: BindingContext,
    binder: AnyBinding<R>,
) : StorageEntryBase<R>(runtime, storageEntryMetadata, api, bindingContext, binder) {

    suspend operator fun invoke(key: K): R? {
        return query(storageEntryMetadata.storageKey(runtime, key))
    }

    fun subscribe(key: K): Flow<R?> {
        return subscribe(storageEntryMetadata.storageKey(runtime, key))
    }
}
