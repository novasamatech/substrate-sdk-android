package jp.co.soramitsu.fearless_utils.runtime

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.json.JsonCodec
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeDefinitionParser
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeDefinitionsTree
import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.DynamicTypeResolver
import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.extentsions.GenericsExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypePreset
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.v13Preset
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.v14Preset
import jp.co.soramitsu.fearless_utils.runtime.definitions.v14.TypesParserV14
import jp.co.soramitsu.fearless_utils.runtime.metadata.Magic
import jp.co.soramitsu.fearless_utils.runtime.metadata.RuntimeMetadata
import jp.co.soramitsu.fearless_utils.runtime.metadata.RuntimeMetadataSchema
import jp.co.soramitsu.fearless_utils.runtime.metadata.builder.V13RuntimeBuilder
import jp.co.soramitsu.fearless_utils.runtime.metadata.builder.V14RuntimeBuilder
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.RuntimeMetadataSchemaV14
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.lookup

class RuntimeFactory(
    private val jsonCodec: JsonCodec
) {

    fun create(
        runtimeMetadata: String,
        typeJsons: List<String> = emptyList()
    ): RuntimeSnapshot {

        val scaleCoderReader = ScaleCodecReader(runtimeMetadata.fromHex())
        val metadataVersion = Magic.read(scaleCoderReader)[Magic.runtimeVersion].toInt()

        val typeRegistry: TypeRegistry
        val metadata: RuntimeMetadata

        when {
            metadataVersion < 14 -> {
                val metadataStruct = RuntimeMetadataSchema.read(scaleCoderReader)
                typeRegistry = constructTypeRegistry(v13Preset(), typeJsons)

                metadata = V13RuntimeBuilder.buildMetadata(
                    metadataStruct = metadataStruct,
                    metadataVersion = metadataVersion,
                    typeRegistry = typeRegistry
                )
            }
            else -> {
                val metadataStruct = RuntimeMetadataSchemaV14.read(scaleCoderReader)
                typeRegistry = constructTypeRegistry(
                    preset = TypesParserV14.parse(metadataStruct.lookup, v14Preset()),
                    typeJsons = typeJsons
                )

                metadata = V14RuntimeBuilder.buildMetadata(
                    metadataStruct = metadataStruct,
                    metadataVersion = metadataVersion,
                    typeRegistry = typeRegistry
                )
            }
        }

        return RuntimeSnapshot(typeRegistry, metadata)
    }

    private fun constructTypeRegistry(
        preset: TypePreset,
        typeJsons: List<String>
    ): TypeRegistry {
        val finalTypes = typeJsons.fold(preset) { accPreset, typesJson ->
            val parsed = jsonCodec.fromJson(typesJson, TypeDefinitionsTree::class.java)

            TypeDefinitionParser.parse(
                tree = parsed,
                typePreset = accPreset,
            )
        }

        return TypeRegistry(
            types = finalTypes,
            dynamicTypeResolver = DynamicTypeResolver(
                DynamicTypeResolver.DEFAULT_COMPOUND_EXTENSIONS + GenericsExtension
            )
        )
    }

    private fun TypeDefinitionParser.parse(
        tree: TypeDefinitionsTree,
        typePreset: TypePreset,
    ): TypePreset {
        val afterBase = parseBaseDefinitions(tree, typePreset)

        return if (tree.versioning.isNullOrEmpty()) {
            afterBase
        } else {
            // TODO allow passing custom runtimeVersion
            parseNetworkVersioning(tree, typePreset)
        }
    }
}

