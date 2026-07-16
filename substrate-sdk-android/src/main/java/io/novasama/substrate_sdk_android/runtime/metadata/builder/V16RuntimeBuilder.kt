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
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.groupByName
import io.novasama.substrate_sdk_android.runtime.metadata.module.Constant
import io.novasama.substrate_sdk_android.runtime.metadata.module.ErrorMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.module.Event
import io.novasama.substrate_sdk_android.runtime.metadata.module.FunctionArgument
import io.novasama.substrate_sdk_android.runtime.metadata.module.MetadataFunction
import io.novasama.substrate_sdk_android.runtime.metadata.module.Module
import io.novasama.substrate_sdk_android.runtime.metadata.module.RuntimeApi
import io.novasama.substrate_sdk_android.runtime.metadata.module.RuntimeApiMethod
import io.novasama.substrate_sdk_android.runtime.metadata.module.RuntimeApiMethodParam
import io.novasama.substrate_sdk_android.runtime.metadata.module.Storage
import io.novasama.substrate_sdk_android.runtime.metadata.module.StorageEntry
import io.novasama.substrate_sdk_android.runtime.metadata.module.StorageEntryType
import io.novasama.substrate_sdk_android.runtime.metadata.module.ViewFunction
import io.novasama.substrate_sdk_android.runtime.metadata.v14.MapTypeV14
import io.novasama.substrate_sdk_android.runtime.metadata.v14.SignedExtensionMetadataV14
import io.novasama.substrate_sdk_android.runtime.metadata.v15.RuntimeApiMethodParamMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.v16.ExtrinsicMetadataV16
import io.novasama.substrate_sdk_android.runtime.metadata.v16.PalletCallMetadataV16
import io.novasama.substrate_sdk_android.runtime.metadata.v16.PalletConstantMetadataV16
import io.novasama.substrate_sdk_android.runtime.metadata.v16.PalletErrorMetadataV16
import io.novasama.substrate_sdk_android.runtime.metadata.v16.PalletEventMetadataV16
import io.novasama.substrate_sdk_android.runtime.metadata.v16.PalletMetadataV16
import io.novasama.substrate_sdk_android.runtime.metadata.v16.PalletViewFunctionMetadataV16
import io.novasama.substrate_sdk_android.runtime.metadata.v16.RuntimeApiMetadataV16
import io.novasama.substrate_sdk_android.runtime.metadata.v16.RuntimeApiMethodMetadataV16
import io.novasama.substrate_sdk_android.runtime.metadata.v16.RuntimeMetadataSchemaV16
import io.novasama.substrate_sdk_android.runtime.metadata.v16.StorageEntryMetadataV16
import io.novasama.substrate_sdk_android.runtime.metadata.v16.StorageMetadataV16
import io.novasama.substrate_sdk_android.scale.EncodableStruct
import java.math.BigInteger

object V16RuntimeBuilder : RuntimeBuilder {

    override fun buildMetadata(
        reader: RuntimeMetadataReader,
        typeRegistry: TypeRegistry,
        fallbackSignedExtensions: List<TransactionExtensionMetadata>,
    ): RuntimeMetadata {
        val metadata = reader.metadataV16

        return RuntimeMetadata(
            metadataVersion = reader.metadataVersion,
            modules = buildModules(metadata[RuntimeMetadataSchemaV16.pallets], typeRegistry),
            extrinsic = buildExtrinsic(metadata[RuntimeMetadataSchemaV16.extrinsic], typeRegistry),
            apis = buildRuntimeApis(metadata[RuntimeMetadataSchemaV16.apis], typeRegistry)
        )
    }

    private fun buildModules(
        modulesRaw: List<EncodableStruct<PalletMetadataV16>>,
        typeRegistry: TypeRegistry
    ): Map<String, Module> {
        return modulesRaw.map {
            buildModule(typeRegistry, it)
        }.groupByName()
    }

    private fun buildModule(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<PalletMetadataV16>,
    ): Module {
        val moduleName = struct[PalletMetadataV16.name]
        val moduleIndex = struct[PalletMetadataV16.index].toInt()

        return Module(
            name = moduleName,
            index = moduleIndex.toBigInteger(),
            storage = struct[PalletMetadataV16.storage]?.let {
                buildStorage(typeRegistry, it, moduleName)
            },
            calls = struct[PalletMetadataV16.calls]?.let {
                buildCalls(typeRegistry, it, moduleIndex)
            },
            events = struct[PalletMetadataV16.events]?.let {
                buildEvents(typeRegistry, it, moduleIndex)
            },
            constants = buildConstants(typeRegistry, struct[PalletMetadataV16.constants]),
            errors = struct[PalletMetadataV16.errors]?.let {
                buildErrors(typeRegistry, it)
            } ?: emptyMap(),
            viewFunctions = buildViewFunctions(
                typeRegistry,
                moduleName,
                struct[PalletMetadataV16.viewFunctions]
            )
        )
    }

    private fun buildStorage(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<StorageMetadataV16>,
        moduleName: String,
    ): Storage {
        val storageEntries = struct[StorageMetadataV16.entries].map { entryStruct ->
            StorageEntry(
                name = entryStruct[StorageEntryMetadataV16.name],
                modifier = entryStruct[StorageEntryMetadataV16.modifier],
                type = buildEntryType(typeRegistry, entryStruct[StorageEntryMetadataV16.type]),
                default = entryStruct[StorageEntryMetadataV16.default],
                documentation = entryStruct[StorageEntryMetadataV16.documentation],
                moduleName = moduleName
            )
        }

        return Storage(
            prefix = struct[StorageMetadataV16.prefix],
            entries = storageEntries.groupByName()
        )
    }

    private fun buildCalls(
        typeRegistry: TypeRegistry,
        callsRaw: EncodableStruct<PalletCallMetadataV16>,
        moduleIndex: Int,
    ): Map<String, MetadataFunction> {
        val type = typeRegistry[callsRaw[PalletCallMetadataV16.type]]

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

    private fun buildEvents(
        typeRegistry: TypeRegistry,
        eventsRaw: EncodableStruct<PalletEventMetadataV16>,
        moduleIndex: Int,
    ): Map<String, Event> {
        val type = typeRegistry[eventsRaw[PalletEventMetadataV16.type]]

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
        constantsRaw: List<EncodableStruct<PalletConstantMetadataV16>>,
    ): Map<String, Constant> {
        return constantsRaw.map { constantStruct ->
            val typeIndex = constantStruct[PalletConstantMetadataV16.type]

            Constant(
                name = constantStruct[PalletConstantMetadataV16.name],
                type = typeRegistry[typeIndex],
                value = constantStruct[PalletConstantMetadataV16.value],
                documentation = constantStruct[PalletConstantMetadataV16.documentation]
            )
        }.groupByName()
    }

    private fun buildErrors(
        typeRegistry: TypeRegistry,
        errorsRaw: EncodableStruct<PalletErrorMetadataV16>,
    ): Map<Int, ErrorMetadata> {
        val type = typeRegistry[errorsRaw[PalletErrorMetadataV16.type]]

        if (type !is DictEnum) return emptyMap()

        return type.elements.entries.map { (variantIndex, variantValue) ->
            ErrorMetadata(
                index = variantIndex,
                name = variantValue.name,
                documentation = emptyList(),
            )
        }.associateBy { it.index }
    }

    private fun buildViewFunctions(
        typeRegistry: TypeRegistry,
        moduleName: String,
        viewFunctionsRaw: List<EncodableStruct<PalletViewFunctionMetadataV16>>,
    ): Map<String, ViewFunction> {
        return viewFunctionsRaw.map { viewFunctionStruct ->
            ViewFunction(
                name = viewFunctionStruct[PalletViewFunctionMetadataV16.name],
                id = viewFunctionStruct[PalletViewFunctionMetadataV16.id],
                palletName = moduleName,
                inputs = viewFunctionStruct[PalletViewFunctionMetadataV16.inputs].map { paramStruct ->
                    RuntimeApiMethodParam(
                        name = paramStruct[RuntimeApiMethodParamMetadata.name],
                        type = typeRegistry[paramStruct[RuntimeApiMethodParamMetadata.type]]
                    )
                },
                output = typeRegistry[viewFunctionStruct[PalletViewFunctionMetadataV16.outputType]]
            )
        }.groupByName()
    }

    private fun buildEntryType(
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

    private fun buildExtrinsic(
        struct: EncodableStruct<ExtrinsicMetadataV16>,
        typeRegistry: TypeRegistry,
    ): ExtrinsicMetadata {
        // v16 exposes a set of supported extrinsic format versions; expose the newest one to stay
        // compatible with the single-versioned domain model.
        val version = struct[ExtrinsicMetadataV16.versions].maxOrNull()?.toInt()?.toBigInteger()
            ?: BigInteger.ZERO

        return ExtrinsicMetadata(
            version = version,
            signedExtensions = struct[ExtrinsicMetadataV16.transactionExtensions].map {
                TransactionExtensionMetadata(
                    id = it[SignedExtensionMetadataV14.identifier],
                    includedInExtrinsic = typeRegistry[it[SignedExtensionMetadataV14.type]],
                    includedInSignature = typeRegistry[it[SignedExtensionMetadataV14.additionalSigned]]
                )
            }
        )
    }

    private fun buildRuntimeApis(
        apisRaw: List<EncodableStruct<RuntimeApiMetadataV16>>,
        typeRegistry: TypeRegistry
    ): List<RuntimeApi> {
        return apisRaw.map { apiStruct ->
            val apiName = apiStruct[RuntimeApiMetadataV16.name]

            RuntimeApi(
                name = apiName,
                methods = apiStruct[RuntimeApiMetadataV16.methods].map { methodStruct ->
                    RuntimeApiMethod(
                        apiName = apiName,
                        name = methodStruct[RuntimeApiMethodMetadataV16.name],
                        inputs = methodStruct[RuntimeApiMethodMetadataV16.inputs].map { paramStruct ->
                            RuntimeApiMethodParam(
                                name = paramStruct[RuntimeApiMethodParamMetadata.name],
                                type = typeRegistry[paramStruct[RuntimeApiMethodParamMetadata.type]]
                            )
                        },
                        output = typeRegistry[methodStruct[RuntimeApiMethodMetadataV16.outputType]]
                    )
                }
            )
        }
    }

    private fun cannotConstructStorageEntry(from: Any?): Nothing {
        throw IllegalArgumentException("Cannot construct StorageEntryType from $from")
    }
}
