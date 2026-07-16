package io.novasama.substrate_sdk_android.runtime.metadata.builder

import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypeRegistry
import io.novasama.substrate_sdk_android.runtime.metadata.ExtrinsicMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadataReader
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.groupByName
import io.novasama.substrate_sdk_android.runtime.metadata.module.Constant
import io.novasama.substrate_sdk_android.runtime.metadata.module.Module
import io.novasama.substrate_sdk_android.runtime.metadata.module.RuntimeApi
import io.novasama.substrate_sdk_android.runtime.metadata.module.RuntimeApiMethod
import io.novasama.substrate_sdk_android.runtime.metadata.module.RuntimeApiMethodParam
import io.novasama.substrate_sdk_android.runtime.metadata.module.Storage
import io.novasama.substrate_sdk_android.runtime.metadata.module.StorageEntry
import io.novasama.substrate_sdk_android.runtime.metadata.module.ViewFunction
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
                PostV14ModuleBuilder.buildCalls(typeRegistry, it[PalletCallMetadataV16.type], moduleIndex)
            },
            events = struct[PalletMetadataV16.events]?.let {
                PostV14ModuleBuilder.buildEvents(typeRegistry, it[PalletEventMetadataV16.type], moduleIndex)
            },
            constants = buildConstants(typeRegistry, struct[PalletMetadataV16.constants]),
            errors = struct[PalletMetadataV16.errors]?.let {
                PostV14ModuleBuilder.buildErrors(typeRegistry, it[PalletErrorMetadataV16.type])
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
                type = PostV14ModuleBuilder.buildEntryType(typeRegistry, entryStruct[StorageEntryMetadataV16.type]),
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
                    resolveParam(typeRegistry, paramStruct)
                },
                output = typeRegistry[viewFunctionStruct[PalletViewFunctionMetadataV16.outputType]]
            )
        }.groupByName()
    }

    private fun buildExtrinsic(
        struct: EncodableStruct<ExtrinsicMetadataV16>,
        typeRegistry: TypeRegistry,
    ): ExtrinsicMetadata {
        // All transaction extensions present across every supported version.
        val transactionExtensions = struct[ExtrinsicMetadataV16.transactionExtensions].map {
            TransactionExtensionMetadata(
                id = it[SignedExtensionMetadataV14.identifier],
                includedInExtrinsic = typeRegistry[it[SignedExtensionMetadataV14.type]],
                includedInSignature = typeRegistry[it[SignedExtensionMetadataV14.additionalSigned]]
            )
        }

        // Which of the above extensions are active for each transaction extension version.
        val transactionExtensionsByVersion = struct[ExtrinsicMetadataV16.transactionExtensionsByVersion]
            .associate { (extensionsVersion, activeIndices) ->
                extensionsVersion.toInt() to activeIndices.map { it.toInt() }
            }

        // All supported extrinsic format versions (distinct from the transaction extension version above).
        val versions = struct[ExtrinsicMetadataV16.versions].map { it.toInt().toBigInteger() }

        return ExtrinsicMetadata(
            versions = versions,
            transactionExtensions = transactionExtensions,
            transactionExtensionsByVersion = transactionExtensionsByVersion
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
                            resolveParam(typeRegistry, paramStruct)
                        },
                        output = typeRegistry[methodStruct[RuntimeApiMethodMetadataV16.outputType]]
                    )
                }
            )
        }
    }

    private fun resolveParam(
        typeRegistry: TypeRegistry,
        paramStruct: EncodableStruct<RuntimeApiMethodParamMetadata>
    ): RuntimeApiMethodParam {
        return RuntimeApiMethodParam(
            name = paramStruct[RuntimeApiMethodParamMetadata.name],
            type = typeRegistry[paramStruct[RuntimeApiMethodParamMetadata.type]]
        )
    }
}
