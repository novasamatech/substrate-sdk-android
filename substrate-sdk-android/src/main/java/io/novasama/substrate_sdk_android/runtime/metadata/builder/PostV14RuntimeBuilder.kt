package io.novasama.substrate_sdk_android.runtime.metadata.builder

import io.novasama.substrate_sdk_android.extensions.requireOrException
import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypeRegistry
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Tuple
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Null
import io.novasama.substrate_sdk_android.runtime.metadata.ExtrinsicMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadataReader
import io.novasama.substrate_sdk_android.runtime.metadata.SignedExtensionMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.groupByName
import io.novasama.substrate_sdk_android.runtime.metadata.module.Constant
import io.novasama.substrate_sdk_android.runtime.metadata.module.Error
import io.novasama.substrate_sdk_android.runtime.metadata.module.Event
import io.novasama.substrate_sdk_android.runtime.metadata.module.FunctionArgument
import io.novasama.substrate_sdk_android.runtime.metadata.module.MetadataFunction
import io.novasama.substrate_sdk_android.runtime.metadata.module.Module
import io.novasama.substrate_sdk_android.runtime.metadata.module.Storage
import io.novasama.substrate_sdk_android.runtime.metadata.module.StorageEntry
import io.novasama.substrate_sdk_android.runtime.metadata.module.StorageEntryType
import io.novasama.substrate_sdk_android.runtime.metadata.v14.MapTypeV14
import io.novasama.substrate_sdk_android.runtime.metadata.v14.PalletCallMetadataV14
import io.novasama.substrate_sdk_android.runtime.metadata.v14.PalletConstantMetadataV14
import io.novasama.substrate_sdk_android.runtime.metadata.v14.PalletErrorMetadataV14
import io.novasama.substrate_sdk_android.runtime.metadata.v14.PalletEventMetadataV14
import io.novasama.substrate_sdk_android.runtime.metadata.v14.PostV14ExtrinsicMetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.v14.PostV14PalletMetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.v14.SignedExtensionMetadataV14
import io.novasama.substrate_sdk_android.runtime.metadata.v14.StorageEntryMetadataV14
import io.novasama.substrate_sdk_android.runtime.metadata.v14.StorageMetadataV14
import io.novasama.substrate_sdk_android.scale.EncodableStruct
import java.math.BigInteger

@OptIn(ExperimentalUnsignedTypes::class)
object PostV14RuntimeBuilder : RuntimeBuilder {

    override fun buildMetadata(
        reader: RuntimeMetadataReader,
        typeRegistry: TypeRegistry,
        knownSignedExtensions: List<SignedExtensionMetadata>,
    ): RuntimeMetadata {
        val metadataStruct = reader.metadata

        val schema = reader.metadataPostV14.schema

        return RuntimeMetadata(
            extrinsic = buildExtrinsic(
                metadataStruct[schema.extrinsic],
                typeRegistry
            ),
            modules = buildModules(metadataStruct[schema.pallets], typeRegistry),
            runtimeVersion = reader.metadataVersion.toBigInteger()
        )
    }

    private fun buildModules(
        modulesRaw: List<EncodableStruct<PostV14PalletMetadataSchema<*>>>,
        typeRegistry: TypeRegistry
    ): Map<String, Module> {
        return modulesRaw.map {
            buildModule(typeRegistry, it)
        }.groupByName()
    }

    private fun buildModule(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<PostV14PalletMetadataSchema<*>>,
    ): Module {
        val schema = struct.schema
        val moduleName = struct[schema.name]
        val moduleIndex = struct[schema.index].toInt()

        return Module(
            name = moduleName,
            index = moduleIndex.toBigInteger(),
            storage = struct[schema.storage]?.let {
                buildStorage(typeRegistry, it, moduleName)
            },
            calls = struct[schema.calls]?.let {
                buildCalls(typeRegistry, it, moduleIndex)
            },
            events = struct[schema.events]?.let {
                buildEvents(typeRegistry, it, moduleIndex)
            },
            constants = buildConstants(typeRegistry, struct[schema.constants]),
            errors = struct[schema.errors]?.let {
                buildErrors(typeRegistry, it)
            } ?: emptyMap()
        )
    }

    private fun buildStorage(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<StorageMetadataV14>,
        moduleName: String,
    ): Storage {
        val storageEntries = struct[StorageMetadataV14.entries].map { entryStruct ->
            StorageEntry(
                name = entryStruct[StorageEntryMetadataV14.name],
                modifier = entryStruct[StorageEntryMetadataV14.modifier],
                type = buildEntryType(typeRegistry, entryStruct[StorageEntryMetadataV14.type]),
                default = entryStruct[StorageEntryMetadataV14.default],
                documentation = entryStruct[StorageEntryMetadataV14.documentation],
                moduleName = moduleName
            )
        }

        return Storage(
            prefix = struct[StorageMetadataV14.prefix],
            entries = storageEntries
                .groupByName()
        )
    }

    private fun buildCalls(
        typeRegistry: TypeRegistry,
        callsRaw: EncodableStruct<PalletCallMetadataV14>,
        moduleIndex: Int,
    ): Map<String, MetadataFunction> {

        val type = typeRegistry[callsRaw[PalletCallMetadataV14.type].toString()]

        if (type !is DictEnum) return emptyMap()

        return type.elements.map { (index, call) ->
            MetadataFunction(
                name = call.name,
                arguments = extractArguments(call.value.value!!) { name, type ->
                    FunctionArgument(name!!, type)
                },
                documentation = emptyList(),
                index = moduleIndex to index
            )
        }.groupByName()
    }

    private fun buildEvents(
        typeRegistry: TypeRegistry,
        eventsRaw: EncodableStruct<PalletEventMetadataV14>,
        moduleIndex: Int,
    ): Map<String, Event> {

        val type = typeRegistry[eventsRaw[PalletEventMetadataV14.type].toString()]

        if (type !is DictEnum) return emptyMap()

        return type.elements.map { (index, event) ->
            Event(
                name = event.name,
                arguments = extractArguments(event.value.value!!) { _, type -> type },
                documentation = emptyList(),
                index = moduleIndex to index
            )
        }.groupByName()
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

    private fun buildConstants(
        typeRegistry: TypeRegistry,
        constantsRaw: List<EncodableStruct<PalletConstantMetadataV14>>,
    ): Map<String, Constant> {

        return constantsRaw.map { constantStruct ->
            val typeIndex = constantStruct[PalletConstantMetadataV14.type].toString()

            Constant(
                name = constantStruct[PalletConstantMetadataV14.name],
                type = typeRegistry[typeIndex],
                value = constantStruct[PalletConstantMetadataV14.value],
                documentation = constantStruct[PalletConstantMetadataV14.documentation]
            )
        }.groupByName()
    }

    private fun buildErrors(
        typeRegistry: TypeRegistry,
        errorsRaw: EncodableStruct<PalletErrorMetadataV14>,
    ): Map<String, Error> {

        val type = typeRegistry[errorsRaw[PalletErrorMetadataV14.type].toString()]

        if (type !is DictEnum) return emptyMap()

        return type.elements.values.map {
            Error(
                name = it.name,
                documentation = emptyList(),
            )
        }.groupByName()
    }

    private fun buildEntryType(
        typeRegistry: TypeRegistry,
        enumValue: Any?
    ): StorageEntryType {
        return when (enumValue) {
            is BigInteger -> {
                StorageEntryType.Plain(typeRegistry[enumValue.toString()])
            }
            is EncodableStruct<*> -> {
                requireOrException(enumValue.schema is MapTypeV14) {
                    cannotConstructStorageEntry(enumValue)
                }

                val hashers = enumValue[MapTypeV14.hashers]

                val type = typeRegistry[enumValue[MapTypeV14.key].toString()]
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
                    typeRegistry[enumValue[MapTypeV14.value].toString()]
                )
            }
            else -> cannotConstructStorageEntry(enumValue)
        }
    }

    private fun buildExtrinsic(
        struct: EncodableStruct<PostV14ExtrinsicMetadataSchema<*>>,
        typeRegistry: TypeRegistry,
    ): ExtrinsicMetadata {
        val schema = struct.schema

        return ExtrinsicMetadata(
            version = struct[schema.version].toInt().toBigInteger(),
            signedExtensions = struct[schema.signedExtensions].map {
                SignedExtensionMetadata(
                    id = it[SignedExtensionMetadataV14.identifier],
                    type = typeRegistry[it[SignedExtensionMetadataV14.type].toString()],
                    additionalSigned = typeRegistry[it[SignedExtensionMetadataV14.additionalSigned].toString()]
                )
            }
        )
    }

    private fun cannotConstructStorageEntry(from: Any?): Nothing {
        throw IllegalArgumentException("Cannot construct StorageEntryType from $from")
    }
}
