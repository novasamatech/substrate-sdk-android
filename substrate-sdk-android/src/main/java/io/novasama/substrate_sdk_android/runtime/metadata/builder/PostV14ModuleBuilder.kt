package io.novasama.substrate_sdk_android.runtime.metadata.builder

import io.novasama.substrate_sdk_android.extensions.requireOrException
import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypeRegistry
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Tuple
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Null
import io.novasama.substrate_sdk_android.runtime.metadata.groupByName
import io.novasama.substrate_sdk_android.runtime.metadata.module.ErrorMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.module.Event
import io.novasama.substrate_sdk_android.runtime.metadata.module.FunctionArgument
import io.novasama.substrate_sdk_android.runtime.metadata.module.MetadataFunction
import io.novasama.substrate_sdk_android.runtime.metadata.module.StorageEntryType
import io.novasama.substrate_sdk_android.runtime.metadata.v14.MapTypeV14
import io.novasama.substrate_sdk_android.scale.EncodableStruct
import java.math.BigInteger

/**
 * Shared, schema-agnostic building blocks for post-v14 metadata (v14, v15, v16).
 *
 * These operate on already-extracted type indices / values rather than on version-specific
 * `EncodableStruct` schemas, so both [V14RuntimeBuilder] and [V16RuntimeBuilder] can reuse them.
 */
internal object PostV14ModuleBuilder {

    fun buildCalls(
        typeRegistry: TypeRegistry,
        callType: BigInteger,
        moduleIndex: Int,
    ): Map<String, MetadataFunction> {
        val type = typeRegistry[callType]

        if (type !is DictEnum) return emptyMap()

        return type.elements.map { (index, call) ->
            MetadataFunction(
                name = call.name,
                arguments = extractArguments(call.value.value!!) { name, argType ->
                    FunctionArgument(name!!, argType)
                },
                documentation = emptyList(),
                index = moduleIndex to index
            )
        }.groupByName()
    }

    fun buildEvents(
        typeRegistry: TypeRegistry,
        eventType: BigInteger,
        moduleIndex: Int,
    ): Map<String, Event> {
        val type = typeRegistry[eventType]

        if (type !is DictEnum) return emptyMap()

        return type.elements.map { (index, event) ->
            Event(
                name = event.name,
                arguments = extractArguments(event.value.value!!) { _, argType -> argType },
                documentation = emptyList(),
                index = moduleIndex to index
            )
        }.groupByName()
    }

    fun buildErrors(
        typeRegistry: TypeRegistry,
        errorType: BigInteger,
    ): Map<Int, ErrorMetadata> {
        val type = typeRegistry[errorType]

        if (type !is DictEnum) return emptyMap()

        return type.elements.entries.map { (variantIndex, variantValue) ->
            ErrorMetadata(
                index = variantIndex,
                name = variantValue.name,
                documentation = emptyList(),
            )
        }.associateBy { it.index }
    }

    fun buildEntryType(
        typeRegistry: TypeRegistry,
        enumValue: Any?
    ): StorageEntryType {
        return when (enumValue) {
            is BigInteger -> {
                StorageEntryType.Plain(typeRegistry[enumValue])
            }

            is EncodableStruct<*> -> {
                requireOrException(enumValue.schema is MapTypeV14) {
                    cannotConstructStorageEntry(enumValue)
                }

                val hashers = enumValue[MapTypeV14.hashers]

                val type = typeRegistry[enumValue[MapTypeV14.key]]
                    ?: cannotConstructStorageEntry(enumValue)

                val keys = if (hashers.size == 1) {
                    listOf(type)
                } else {
                    if (type is Tuple) {
                        type.typeReferences.mapNotNull(TypeReference::value)
                    } else {
                        cannotConstructStorageEntry(enumValue)
                    }
                }

                requireOrException(keys.size == hashers.size) {
                    cannotConstructStorageEntry(enumValue)
                }

                StorageEntryType.NMap(
                    keys,
                    hashers,
                    typeRegistry[enumValue[MapTypeV14.value]]
                )
            }

            else -> cannotConstructStorageEntry(enumValue)
        }
    }

    fun cannotConstructStorageEntry(from: Any?): Nothing {
        throw IllegalArgumentException("Cannot construct StorageEntryType from $from")
    }

    private fun <T> extractArguments(
        type: Type<*>,
        mapper: (name: String?, type: Type<*>?) -> T
    ): List<T> {
        return when (type) {
            is Null -> emptyList()
            is Tuple -> type.typeReferences.map { typeRef ->
                mapper(null, typeRef.value)
            }
            is Struct -> type.mapping.map { mapEntry ->
                mapper(mapEntry.key, mapEntry.value.value)
            }
            else -> listOf(mapper(null, type))
        }
    }
}
