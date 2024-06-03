package io.novasama.substrate_sdk_android.runtime.metadata.builder

import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypeRegistry
import io.novasama.substrate_sdk_android.runtime.metadata.DoubleMapSchema
import io.novasama.substrate_sdk_android.runtime.metadata.ErrorMetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.EventMetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.ExtrinsicMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.ExtrinsicMetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.FunctionArgumentMetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.FunctionMetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.MapSchema
import io.novasama.substrate_sdk_android.runtime.metadata.ModuleConstantMetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.ModuleMetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.NMapSchema
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadataReader
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.SignedExtensionMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.StorageEntryMetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.StorageMetadataSchema
import io.novasama.substrate_sdk_android.runtime.metadata.groupByName
import io.novasama.substrate_sdk_android.runtime.metadata.module.Constant
import io.novasama.substrate_sdk_android.runtime.metadata.module.ErrorMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.module.Event
import io.novasama.substrate_sdk_android.runtime.metadata.module.FunctionArgument
import io.novasama.substrate_sdk_android.runtime.metadata.module.MetadataFunction
import io.novasama.substrate_sdk_android.runtime.metadata.module.Module
import io.novasama.substrate_sdk_android.runtime.metadata.module.Storage
import io.novasama.substrate_sdk_android.runtime.metadata.module.StorageEntry
import io.novasama.substrate_sdk_android.runtime.metadata.module.StorageEntryType
import io.novasama.substrate_sdk_android.scale.EncodableStruct

@OptIn(ExperimentalUnsignedTypes::class)
internal object V13RuntimeBuilder : RuntimeBuilder {

    override fun buildMetadata(
        reader: RuntimeMetadataReader,
        typeRegistry: TypeRegistry,
        knownSignedExtensions: List<SignedExtensionMetadata>
    ): RuntimeMetadata {
        val metadataStruct = reader.metadata

        require(metadataStruct.schema is RuntimeMetadataSchema)

        return RuntimeMetadata(
            extrinsic = buildExtrinsic(
                struct = metadataStruct[RuntimeMetadataSchema.extrinsic],
                knownSignedExtensions = knownSignedExtensions
            ),
            modules = buildModules(metadataStruct[RuntimeMetadataSchema.modules], typeRegistry),
            metadataVersion = reader.metadataVersion
        )
    }

    private fun buildModules(
        modulesRaw: List<EncodableStruct<ModuleMetadataSchema>>,
        typeRegistry: TypeRegistry
    ): Map<String, Module> {
        return modulesRaw.map {
            buildModule(typeRegistry, it)
        }.groupByName()
    }

    private fun buildModule(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<ModuleMetadataSchema>,
    ): Module {
        val moduleName = struct[ModuleMetadataSchema.name]
        val moduleIndex = struct[ModuleMetadataSchema.index].toInt()

        return Module(
            name = moduleName,
            index = moduleIndex.toBigInteger(),
            storage = struct[ModuleMetadataSchema.storage]?.let {
                buildStorage(typeRegistry, it, moduleName)
            },
            calls = struct[ModuleMetadataSchema.calls]?.let {
                buildCalls(typeRegistry, it, moduleIndex)
            },
            events = struct[ModuleMetadataSchema.events]?.let {
                buildEvents(typeRegistry, it, moduleIndex)
            },
            constants = buildConstants(typeRegistry, struct[ModuleMetadataSchema.constants]),
            errors = buildErrors(struct[ModuleMetadataSchema.errors])
        )
    }

    private fun buildStorage(
        typeRegistry: TypeRegistry,
        struct: EncodableStruct<StorageMetadataSchema>,
        moduleName: String,
    ): Storage {
        val storageEntries = struct[StorageMetadataSchema.entries].map { entryStruct ->
            StorageEntry(
                name = entryStruct[StorageEntryMetadataSchema.name],
                modifier = entryStruct[StorageEntryMetadataSchema.modifier],
                type = buildEntryType(typeRegistry, entryStruct[StorageEntryMetadataSchema.type]),
                default = entryStruct[StorageEntryMetadataSchema.default],
                documentation = entryStruct[StorageEntryMetadataSchema.documentation],
                moduleName = moduleName
            )
        }

        return Storage(
            prefix = struct[StorageMetadataSchema.prefix],
            entries = storageEntries
                .groupByName()
        )
    }

    private fun buildCalls(
        typeRegistry: TypeRegistry,
        callsRaw: List<EncodableStruct<FunctionMetadataSchema>>,
        moduleIndex: Int,
    ): Map<String, MetadataFunction> {

        return callsRaw.mapIndexed { index, callStruct ->
            MetadataFunction(
                name = callStruct[FunctionMetadataSchema.name],
                arguments = callStruct[FunctionMetadataSchema.arguments].map { argumentStruct ->
                    FunctionArgument(
                        name = argumentStruct[FunctionArgumentMetadataSchema.name],
                        type = typeRegistry[argumentStruct[FunctionArgumentMetadataSchema.type]]
                    )
                },
                documentation = callStruct[FunctionMetadataSchema.documentation],
                index = moduleIndex to index
            )
        }.groupByName()
    }

    private fun buildEvents(
        typeRegistry: TypeRegistry,
        eventsRaw: List<EncodableStruct<EventMetadataSchema>>,
        moduleIndex: Int,
    ): Map<String, Event> {

        return eventsRaw.mapIndexed { index, eventStruct ->
            Event(
                name = eventStruct[EventMetadataSchema.name],
                arguments = eventStruct[EventMetadataSchema.arguments].map { typeRegistry[it] },
                documentation = eventStruct[EventMetadataSchema.documentation],
                index = moduleIndex to index
            )
        }.groupByName()
    }

    private fun buildConstants(
        typeRegistry: TypeRegistry,
        constantsRaw: List<EncodableStruct<ModuleConstantMetadataSchema>>,
    ): Map<String, Constant> {

        return constantsRaw.map { constantStruct ->
            Constant(
                name = constantStruct[ModuleConstantMetadataSchema.name],
                type = typeRegistry[constantStruct[ModuleConstantMetadataSchema.type]],
                value = constantStruct[ModuleConstantMetadataSchema.value],
                documentation = constantStruct[ModuleConstantMetadataSchema.documentation]
            )
        }.groupByName()
    }

    private fun buildErrors(
        errorsRaw: List<EncodableStruct<ErrorMetadataSchema>>,
    ): Map<Int, ErrorMetadata> {
        return errorsRaw.mapIndexed { index, errorStruct ->
            ErrorMetadata(
                index = index,
                name = errorStruct[ErrorMetadataSchema.name],
                documentation = errorStruct[ErrorMetadataSchema.documentation]
            )
        }.associateBy { it.index }
    }

    private fun buildEntryType(
        typeRegistry: TypeRegistry,
        enumValue: Any?
    ): StorageEntryType {
        return when (enumValue) {
            is String -> {
                StorageEntryType.Plain(typeRegistry[enumValue])
            }
            is EncodableStruct<*> -> {
                when (enumValue.schema) {
                    MapSchema -> StorageEntryType.NMap(
                        keys = listOf(typeRegistry[enumValue[MapSchema.key]]),
                        hashers = listOf(enumValue[MapSchema.hasher]),
                        value = typeRegistry[enumValue[MapSchema.value]]
                    )
                    DoubleMapSchema -> StorageEntryType.NMap(
                        keys = listOf(
                            typeRegistry[enumValue[DoubleMapSchema.key1]],
                            typeRegistry[enumValue[DoubleMapSchema.key2]],
                        ),
                        hashers = listOf(
                            enumValue[DoubleMapSchema.key1Hasher],
                            enumValue[DoubleMapSchema.key2Hasher],
                        ),
                        value = typeRegistry[enumValue[DoubleMapSchema.value]]
                    )
                    NMapSchema -> StorageEntryType.NMap(
                        keys = enumValue[NMapSchema.keys].map { typeRegistry[it] },
                        hashers = enumValue[NMapSchema.hashers],
                        value = typeRegistry[enumValue[NMapSchema.value]]
                    )
                    else -> cannotConstructStorageEntry(enumValue)
                }
            }
            else -> cannotConstructStorageEntry(enumValue)
        }
    }

    private fun buildExtrinsic(
        struct: EncodableStruct<ExtrinsicMetadataSchema>,
        knownSignedExtensions: List<SignedExtensionMetadata>
    ): ExtrinsicMetadata {
        val knownSignedExtensionsById = knownSignedExtensions.associateBy { it.id }

        return ExtrinsicMetadata(
            version = struct[ExtrinsicMetadataSchema.version].toInt().toBigInteger(),
            signedExtensions = struct[ExtrinsicMetadataSchema.signedExtensions].mapNotNull {
                knownSignedExtensionsById[it]
            }
        )
    }

    private fun cannotConstructStorageEntry(from: Any?): Nothing {
        throw IllegalArgumentException("Cannot construct StorageEntryType from $from")
    }
}
