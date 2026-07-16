package io.novasama.substrate_sdk_android.runtime.metadata.builder

import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypeRegistry
import io.novasama.substrate_sdk_android.runtime.metadata.ExtrinsicMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadataReader
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.groupByName
import io.novasama.substrate_sdk_android.runtime.metadata.module.Constant
import io.novasama.substrate_sdk_android.runtime.metadata.module.Module
import io.novasama.substrate_sdk_android.runtime.metadata.module.Storage
import io.novasama.substrate_sdk_android.runtime.metadata.module.StorageEntry
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

object V14RuntimeBuilder : RuntimeBuilder {

    override fun buildMetadata(
        reader: RuntimeMetadataReader,
        typeRegistry: TypeRegistry,
        fallbackSignedExtensions: List<TransactionExtensionMetadata>,
    ): RuntimeMetadata {
        val metadataStruct = reader.metadata

        val schema = reader.metadataPostV14.schema

        return RuntimeMetadata(
            extrinsic = buildExtrinsic(
                metadataStruct[schema.extrinsic],
                typeRegistry
            ),
            modules = buildModules(metadataStruct[schema.pallets], typeRegistry),
            metadataVersion = reader.metadataVersion,
            apis = null
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
                PostV14ModuleBuilder.buildCalls(typeRegistry, it[PalletCallMetadataV14.type], moduleIndex)
            },
            events = struct[schema.events]?.let {
                PostV14ModuleBuilder.buildEvents(typeRegistry, it[PalletEventMetadataV14.type], moduleIndex)
            },
            constants = buildConstants(typeRegistry, struct[schema.constants]),
            errors = struct[schema.errors]?.let {
                PostV14ModuleBuilder.buildErrors(typeRegistry, it[PalletErrorMetadataV14.type])
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
                type = PostV14ModuleBuilder.buildEntryType(typeRegistry, entryStruct[StorageEntryMetadataV14.type]),
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

    private fun buildConstants(
        typeRegistry: TypeRegistry,
        constantsRaw: List<EncodableStruct<PalletConstantMetadataV14>>,
    ): Map<String, Constant> {
        return constantsRaw.map { constantStruct ->
            val typeIndex = constantStruct[PalletConstantMetadataV14.type]

            Constant(
                name = constantStruct[PalletConstantMetadataV14.name],
                type = typeRegistry[typeIndex],
                value = constantStruct[PalletConstantMetadataV14.value],
                documentation = constantStruct[PalletConstantMetadataV14.documentation]
            )
        }.groupByName()
    }

    private fun buildExtrinsic(
        struct: EncodableStruct<PostV14ExtrinsicMetadataSchema<*>>,
        typeRegistry: TypeRegistry,
    ): ExtrinsicMetadata {
        val schema = struct.schema

        return ExtrinsicMetadata(
            version = struct[schema.version].toInt().toBigInteger(),
            signedExtensions = struct[schema.signedExtensions].map {
                TransactionExtensionMetadata(
                    id = it[SignedExtensionMetadataV14.identifier],
                    includedInExtrinsic = typeRegistry[it[SignedExtensionMetadataV14.type]],
                    includedInSignature = typeRegistry[it[SignedExtensionMetadataV14.additionalSigned]]
                )
            }
        )
    }
}
