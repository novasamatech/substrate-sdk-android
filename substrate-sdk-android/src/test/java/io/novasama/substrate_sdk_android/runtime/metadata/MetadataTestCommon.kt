package io.novasama.substrate_sdk_android.runtime.metadata

import io.novasama.substrate_sdk_android.getFileContentFromResources
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.DynamicTypeResolver
import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypeRegistry
import io.novasama.substrate_sdk_android.runtime.definitions.registry.v14Preset
import io.novasama.substrate_sdk_android.runtime.definitions.v14.TypesParserV14
import io.novasama.substrate_sdk_android.runtime.metadata.builder.VersionedRuntimeBuilder
import io.novasama.substrate_sdk_android.runtime.metadata.v14.RuntimeMetadataSchemaV14

object MetadataTestCommon {

    fun buildPost14TestRuntime(fileName: String): RuntimeSnapshot {
        val inHex = getFileContentFromResources(fileName)
        val metadataReader = RuntimeMetadataReader.read(inHex)

        val schema = metadataReader.metadataPostV14.schema

        val typePreset = TypesParserV14.parse(
            lookup = metadataReader.metadata[schema.lookup],
            typePreset = v14Preset()
        )

        val typeRegistry = TypeRegistry(
            typePreset,
            DynamicTypeResolver.defaultCompoundResolver()
        )
        val metadata = VersionedRuntimeBuilder.buildMetadata(metadataReader, typeRegistry)

        return RuntimeSnapshot(typeRegistry, metadata)
    }
}
