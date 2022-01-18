package jp.co.soramitsu.fearless_utils.runtime

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import jp.co.soramitsu.fearless_utils.getFileContentFromResources
import jp.co.soramitsu.fearless_utils.getResourceReader
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeDefinitionParser
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeDefinitionsTree
import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.DynamicTypeResolver
import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.extentsions.GenericsExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.v13Preset
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.v14Preset
import jp.co.soramitsu.fearless_utils.runtime.definitions.v14.TypesParserV14
import jp.co.soramitsu.fearless_utils.runtime.metadata.RuntimeMetadataReader
import jp.co.soramitsu.fearless_utils.runtime.metadata.builder.VersionedRuntimeBuilder
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.RuntimeMetadataSchemaV14

object RealRuntimeProvider {

    fun buildRuntime(networkName: String): RuntimeSnapshot {
        val metadataRaw = buildRawMetadata(networkName)
        val typeRegistry = buildRegistry(networkName)

        val metadata = VersionedRuntimeBuilder.buildMetadata(metadataRaw, typeRegistry)

        return RuntimeSnapshot(typeRegistry, metadata)
    }

    fun buildRuntimeV14(networkName: String): RuntimeSnapshot {
        val gson = Gson()

        val metadataRaw = buildRawMetadata(networkName)

        val metadataTypePreset = TypesParserV14.parse(metadataRaw.metadata[RuntimeMetadataSchemaV14.lookup], v14Preset())

        val networkTypesReader = JsonReader(getResourceReader("${networkName}.json"))
        val networkTypesTree = gson.fromJson<TypeDefinitionsTree>(networkTypesReader, TypeDefinitionsTree::class.java)

        val completeTypes = TypeDefinitionParser.parseBaseDefinitions(networkTypesTree, metadataTypePreset)

        val typeRegistry = TypeRegistry(
            types = completeTypes,
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