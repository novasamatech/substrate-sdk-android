package jp.co.soramitsu.fearless_utils.decoratable_api.query

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.state.getStorage
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.state.state
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.state.subscribeStorage
import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.AnyBinding
import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.BindingContext
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encodeToDynamicStructure
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.StorageEntry
import jp.co.soramitsu.fearless_utils.runtime.metadata.storageKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.reflect.KType

abstract class StorageEntryBase<R>(
    protected val runtime: RuntimeSnapshot,
    protected val storageEntryMetadata: StorageEntry,
    private val api: SubstrateApi,
    private val bindingContext: BindingContext,
    val binder: AnyBinding<R>,
) {
    protected fun encodeKey(key: Any?, keyType: KType) = bindingContext.scale.encodeToDynamicStructure(keyType, key)

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

class StorageEntry0<R>(
    runtime: RuntimeSnapshot,
    storageEntryMetadata: StorageEntry,
    api: SubstrateApi,
    bindingContext: BindingContext,
    binder: AnyBinding<R>,
) : StorageEntryBase<R>(runtime, storageEntryMetadata, api, bindingContext, binder) {

    suspend operator fun invoke(): R? = query(storageKey())

    fun subscribe(): Flow<R?> = subscribe(storageKey())

    private fun storageKey() = storageEntryMetadata.storageKey()
}

class StorageEntry1<K, R>(
    runtime: RuntimeSnapshot,
    storageEntryMetadata: StorageEntry,
    api: SubstrateApi,
    bindingContext: BindingContext,
    binder: AnyBinding<R>,
    private val a1Type: KType,
    ) : StorageEntryBase<R>(runtime, storageEntryMetadata, api, bindingContext, binder) {

    suspend operator fun invoke(key: K): R? {
        return query(storageKey(key))
    }

    fun subscribe(key: K): Flow<R?> {
        return subscribe(storageKey(key))
    }

    private fun storageKey(key: K): String {
        return storageEntryMetadata.storageKey(
            runtime,
            encodeKey(key, a1Type)
        )
    }
}

class StorageEntry2<K1, K2, R>(
    runtime: RuntimeSnapshot,
    storageEntryMetadata: StorageEntry,
    api: SubstrateApi,
    bindingContext: BindingContext,
    binder: AnyBinding<R>,
    private val a1Type: KType,
    private val a2Type: KType,
) : StorageEntryBase<R>(runtime, storageEntryMetadata, api, bindingContext, binder) {

    suspend operator fun invoke(key1: K1, key2: K2): R? {
        return query(storageKey(key1, key2))
    }

    fun subscribe(key1: K1, key2: K2): Flow<R?> {
        return subscribe(storageKey(key1, key2))
    }

    private fun storageKey(key1: K1, key2: K2): String {
        return storageEntryMetadata.storageKey(
            runtime,
            encodeKey(key1, a1Type),
            encodeKey(key2, a2Type)
        )
    }
}

class StorageEntry3<K1, K2, K3, R>(
    runtime: RuntimeSnapshot,
    storageEntryMetadata: StorageEntry,
    api: SubstrateApi,
    bindingContext: BindingContext,
    binder: AnyBinding<R>,
    private val a1Type: KType,
    private val a2Type: KType,
    private val a3Type: KType,
) : StorageEntryBase<R>(runtime, storageEntryMetadata, api, bindingContext, binder) {

    suspend operator fun invoke(key1: K1, key2: K2, key3: K3): R? {
        return query(storageKey(key1, key2, key3))
    }

    fun subscribe(key1: K1, key2: K2, key3: K3): Flow<R?> {
        return subscribe(storageKey(key1, key2, key3))
    }

    private fun storageKey(key1: K1, key2: K2, key3: K3): String {
        return storageEntryMetadata.storageKey(
            runtime,
            encodeKey(key1, a1Type),
            encodeKey(key2, a2Type),
            encodeKey(key3, a3Type),
        )
    }
}
