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
import jp.co.soramitsu.fearless_utils.runtime.metadata.RuntimeMetadataReader
import jp.co.soramitsu.fearless_utils.runtime.metadata.builder.VersionedRuntimeBuilder

object RealRuntimeProvider {

    fun buildRuntime(networkName: String): RuntimeSnapshot {
        val metadataRaw = buildRawMetadata(networkName)
        val typeRegistry = buildRegistry(networkName)

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

        val defaultTypeRegistry =
            TypeDefinitionParser.parseBaseDefinitions(tree, v13Preset()).typePreset
        val networkParsed = TypeDefinitionParser.parseNetworkVersioning(
            kusamaTree,
            defaultTypeRegistry
        )

        return TypeRegistry(
            types = networkParsed.typePreset,
            dynamicTypeResolver = DynamicTypeResolver(
                DynamicTypeResolver.DEFAULT_COMPOUND_EXTENSIONS + GenericsExtension
            )
        )
    }
}