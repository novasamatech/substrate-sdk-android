package io.novasama.substrate_sdk_android.runtime.metadata.module

import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.metadata.StorageEntryModifier
import io.novasama.substrate_sdk_android.runtime.metadata.StorageHasher
import io.novasama.substrate_sdk_android.runtime.metadata.WithName
import java.math.BigInteger

class Module(
    override val name: String,
    val storage: Storage?,
    val calls: Map<String, MetadataFunction>?,
    val events: Map<String, Event>?,
    val constants: Map<String, Constant>,
    val errors: Map<Int, ErrorMetadata>,
    val index: BigInteger
) : WithName

class Storage(
    val prefix: String,
    val entries: Map<String, StorageEntry>
) {

    operator fun get(entry: String) = entries[entry]
}

class StorageEntry(
    override val name: String,
    val modifier: StorageEntryModifier,
    val type: StorageEntryType,
    val default: ByteArray,
    val documentation: List<String>,
    val moduleName: String
) : WithName

sealed class StorageEntryType(
    val value: Type<*>?
) {

    class Plain(value: Type<*>?) : StorageEntryType(value)

    class NMap(
        val keys: List<Type<*>?>,
        val hashers: List<StorageHasher>,
        value: Type<*>?
    ) : StorageEntryType(value)
}

class MetadataFunction(
    override val name: String,
    val arguments: List<FunctionArgument>,
    val documentation: List<String>,
    val index: Pair<Int, Int>
) : WithName

class FunctionArgument(
    override val name: String,
    val type: Type<*>?
) : WithName

class Event(
    override val name: String,
    val index: Pair<Int, Int>,
    val arguments: List<Type<*>?>,
    val documentation: List<String>
) : WithName

class Constant(
    override val name: String,
    val type: Type<*>?,
    val value: ByteArray,
    val documentation: List<String>
) : WithName

class ErrorMetadata(
    val index: Int,
    override val name: String,
    val documentation: List<String>
) : WithName
