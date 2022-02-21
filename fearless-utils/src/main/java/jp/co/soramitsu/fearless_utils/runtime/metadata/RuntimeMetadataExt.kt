package jp.co.soramitsu.fearless_utils.runtime.metadata

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import jp.co.soramitsu.fearless_utils.hash.Hasher.xxHash128
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.RuntimeType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.bytes
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.errors.EncodeDecodeException
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Event
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.MetadataFunction
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Module
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.StorageEntry
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.StorageEntryType
import java.io.ByteArrayOutputStream

/**
 * @throws NoSuchElementException if module was not found
 */
fun RuntimeMetadata.module(index: Int): Module =
    modules.values.first { it.index == index.toBigInteger() }

fun RuntimeMetadata.moduleOrNull(index: Int): Module? = nullOnException { module(index) }

/**
 * @throws NoSuchElementException if module was not found
 */
fun RuntimeMetadata.module(name: String) = moduleOrNull(name) ?: throw NoSuchElementException()

fun RuntimeMetadata.moduleOrNull(name: String): Module? = modules[name]

/**
 * @throws NoSuchElementException if storage entry was not found
 */
fun Module.storage(name: String): StorageEntry =
    storageOrNull(name) ?: throw NoSuchElementException()

fun Module.storageOrNull(name: String): StorageEntry? = storage?.get(name)

/**
 * @throws NoSuchElementException if call was not found
 */
fun Module.call(index: Int): MetadataFunction = requireElementInMap(calls, index)

fun Module.callOrNull(index: Int): MetadataFunction? = nullOnException { call(index) }

/**
 * @throws NoSuchElementException if call was not found
 */
fun Module.call(name: String): MetadataFunction = callOrNull(name) ?: throw NoSuchElementException()

fun Module.callOrNull(name: String): MetadataFunction? = calls?.get(name)

/**
 * @throws NoSuchElementException if event was not found
 */
fun Module.event(index: Int): Event = requireElementInMap(events, index)

fun Module.eventOrNull(index: Int): Event? = nullOnException { event(index) }

/**
 * @throws NoSuchElementException if event was not found
 */
fun Module.event(name: String): Event = eventOrNull(name) ?: throw NoSuchElementException()

fun Module.eventOrNull(name: String): Event? = events?.get(name)

/**
 * Unified representation of [StorageEntryType] argument types
 */
val StorageEntry.keys : List<RuntimeType<*, *>?>
    get() = when(type) {
        is StorageEntryType.Plain -> emptyList()
        is StorageEntryType.NMap -> type.keys
    }

/**
 * Unified representation of [StorageEntryType] hashers
 */
val StorageEntry.hashers : List<StorageHasher>
    get() = when(type) {
        is StorageEntryType.Plain -> emptyList()
        is StorageEntryType.NMap -> type.hashers
    }

/**
 * Constructs a key for storage with no arguments.
 * This either fill be a full key for [StorageEntryType.Plain] entries,
 * or a prefix key for [StorageEntryType.Map] and [StorageEntryType.DoubleMap] entries
 *
 */
fun StorageEntry.storageKey(): String {
    return (moduleHash() + serviceHash()).toHexString(withPrefix = true)
}

fun StorageEntry.storageKeyOrNull() = nullOnException { storageKey() }

/**
 * Dimension of [StorageEntryType] is an number of arguments of which the key is formed
 */
fun StorageEntryType.dimension() = when (this) {
    is StorageEntryType.Plain -> 0
    is StorageEntryType.NMap -> keys.size
}

/**
 * Constructs a key for storage with supplied arguments.
 *
 * If [StorageEntryType.dimension] is equal to the number of arguments, then result will be the full storage key
 * If [StorageEntryType.dimension] is greater then the number of arguments, then result will be the prefix key
 *
 * @throws IllegalArgumentException if [StorageEntryType.dimension] is less than the number of arguments
 * @throws IllegalStateException if some of types used for encoding cannot be resolved
 * @throws EncodeDecodeException if error happened during encoding
 */
fun StorageEntry.storageKey(runtime: RuntimeSnapshot, vararg arguments: Any?): String {
    // keys size can be less then dimension to retrieve by prefix
    if (arguments.size > type.dimension()) wrongEntryType()

    val argumentsTypes = this.keys
    val argumentsHashers = this.hashers

    val keyOutputStream = ByteArrayOutputStream()

    keyOutputStream.write(moduleHash())
    keyOutputStream.write(serviceHash())

    arguments.forEachIndexed { index, key ->
        val argumentType = argumentsTypes[index]
        val argumentHasher = argumentsHashers[index]

        val keyEncoded = argumentType?.bytes(runtime, key) ?: typeNotResolved(fullName)

        keyOutputStream.write(argumentHasher.hashingFunction(keyEncoded))
    }

    return keyOutputStream.toByteArray().toHexString(withPrefix = true)
}

/**
 * Constructs multiple keys for storage with supplied arguments.
 * This method cannot be used to construct prefix keys, for prefix construction see [StorageEntry.storageKey]
 *
 * @throws IllegalArgumentException if the number of arguments is not equal to [StorageEntryType.dimension]
 * @throws IllegalStateException if some of types used for encoding cannot be resolved
 * @throws EncodeDecodeException if error happened during encoding
 */
fun StorageEntry.storageKeys(runtime: RuntimeSnapshot, keysArguments: List<List<Any?>>): List<String> {
    val argumentsTypes = this.keys
    val argumentsHashers = this.hashers

    val moduleHash = moduleHash()
    val storageHash = serviceHash()

    return keysArguments.map { arguments ->
        if (arguments.size != type.dimension()) wrongEntryType()

        val keyOutputStream = ByteArrayOutputStream()

        keyOutputStream.write(moduleHash)
        keyOutputStream.write(storageHash)

        arguments.forEachIndexed { index, key ->
            val argumentType = argumentsTypes[index]
            val argumentHasher = argumentsHashers[index]

            val keyEncoded = argumentType?.bytes(runtime, key) ?: typeNotResolved(fullName)

            keyOutputStream.write(argumentHasher.hashingFunction(keyEncoded))
        }

        keyOutputStream.toByteArray().toHexString(withPrefix = true)
    }
}

private const val MODULE_HASH_LENGTH = 16
private const val CALL_HASH_LENGTH = MODULE_HASH_LENGTH

/**
 * Splits scale-encoded full storage key into its components (arguments)
 * @throws IllegalStateException - in case of non-concat hasher or unknown type for some argument
 * @throws IllegalArgumentException - in case storage has plain type
 */
// layout: <moduleHash><callHash><key1Hash><key1? if concat>...<keyNHash><keyN? if concat>
fun StorageEntry.splitKey(runtime: RuntimeSnapshot, fullKey: String): List<Any?> {
    val scaleReader = ScaleCodecReader(fullKey.fromHex())
    val entryType = type

    require(entryType is StorageEntryType.NMap) {
        "Cannot split arguments for plain-storage key"
    }

    scaleReader.skip(MODULE_HASH_LENGTH)
    scaleReader.skip(CALL_HASH_LENGTH)

    return entryType.keys.zip(entryType.hashers).mapIndexed { index, (key, hasher) ->
        val hashSize = when (hasher) {
            StorageHasher.Blake2_128Concat -> 16
            StorageHasher.Twox64Concat -> 8
            StorageHasher.Identity -> 0
            else -> error("Cannot extract argument with non-concat hasher")
        }

        scaleReader.skip(hashSize)

        key?.decode(scaleReader, runtime)
            ?: error("Unknown type for argument at position $index in $fullName")
    }
}

fun StorageEntry.storageKeyOrNull(runtime: RuntimeSnapshot, vararg keys: Any?): String? {
    return nullOnException { storageKey(runtime, keys) }
}

fun Module.fullNameOf(suffix: String): String {
    return "$name.$suffix"
}

fun Module.fullNameOf(withName: WithName): String {
    return "$name.${withName.name}"
}

val StorageEntry.fullName
    get() = "$moduleName.$name"

private fun typeNotResolved(entryName: String): Nothing =
    throw IllegalStateException("Cannot resolve key or value type for storage entry `$entryName`")

private fun wrongEntryType(): Nothing =
    throw IllegalArgumentException("Storage entry has different type than requested for storage key")

private fun StorageEntry.moduleHash() = moduleName.toByteArray().xxHash128()

private fun StorageEntry.serviceHash() = name.toByteArray().xxHash128()

private inline fun <T> nullOnException(block: () -> T): T? {
    return runCatching(block).getOrNull()
}

private fun <V> requireElementInMap(map: Map<String, V>?, index: Int): V {
    if (map == null) throw NoSuchElementException()

    return map.values.elementAtOrNull(index) ?: throw NoSuchElementException()
}
