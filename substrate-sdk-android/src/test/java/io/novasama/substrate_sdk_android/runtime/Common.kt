package io.novasama.substrate_sdk_android.runtime

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import io.novasama.substrate_sdk_android.getFileContentFromResources
import io.novasama.substrate_sdk_android.getResourceReader
import io.novasama.substrate_sdk_android.runtime.definitions.TypeDefinitionParser
import io.novasama.substrate_sdk_android.runtime.definitions.TypeDefinitionsTree
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.DynamicTypeResolver
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.extentsions.GenericsExtension
import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypeRegistry
import io.novasama.substrate_sdk_android.runtime.definitions.registry.v13Preset
import io.novasama.substrate_sdk_android.runtime.definitions.registry.v14Preset
import io.novasama.substrate_sdk_android.runtime.definitions.v14.TypesParserV14
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadataReader
import io.novasama.substrate_sdk_android.runtime.metadata.builder.VersionedRuntimeBuilder
import io.novasama.substrate_sdk_android.runtime.metadata.v14.RuntimeMetadataSchemaV14

object RealRuntimeProvider {

    fun buildRuntime(networkName: String): RuntimeSnapshot {
        val metadataRaw = buildRawMetadata(networkName)
        val typeRegistry = buildRegistry(networkName)

        val metadata = VersionedRuntimeBuilder.buildMetadata(metadataRaw, typeRegistry)

        return RuntimeSnapshot(typeRegistry, metadata)
    }

    fun buildRuntimePostV14(networkName: String): RuntimeSnapshot {
        val metadataRaw = buildRawMetadata(networkName)

        val postV14Metadata = metadataRaw.metadataPostV14
        val postV14Schema = postV14Metadata.schema

        val metadataTypePreset = TypesParserV14.parse(postV14Metadata[postV14Schema.lookup], v14Preset())

        val typeRegistry = TypeRegistry(
            types = metadataTypePreset,
            dynamicTypeResolver = DynamicTypeResolver(
                DynamicTypeResolver.DEFAULT_COMPOUND_EXTENSIONS + GenericsExtension
            )
        )
        val metadata = VersionedRuntimeBuilder.buildMetadata(metadataRaw, typeRegistry)

        return RuntimeSnapshot(typeRegistry, metadata)
    }

    fun buildRawMetadata(networkName: String = "kusama") =
        getFileContentFromResources("${networkName}_metadata").run {
            RuntimeMetadataReader.read(this)
        }

    fun buildRegistry(networkName: String): TypeRegistry {
        val gson = Gson()
        val reader = JsonReader(getResourceReader("default.json"))
        val kusamaReader = JsonReader(getResourceReader("${networkName}.json"))

        val tree = gson.fromJson<TypeDefinitionsTree>(reader, TypeDefinitionsTree::class.java)
        val kusamaTree =
            gson.fromJson<TypeDefinitionsTree>(kusamaReader, TypeDefinitionsTree::class.java)

        val defaultTypeRegistry = TypeDefinitionParser.parseBaseDefinitions(tree, v13Preset())
        val networkParsed = TypeDefinitionParser.parseNetworkVersioning(
            kusamaTree,
            defaultTypeRegistry
        )

        return TypeRegistry(
            types = networkParsed,
            dynamicTypeResolver = DynamicTypeResolver(
                DynamicTypeResolver.DEFAULT_COMPOUND_EXTENSIONS + GenericsExtension
            )
        )
    }
}