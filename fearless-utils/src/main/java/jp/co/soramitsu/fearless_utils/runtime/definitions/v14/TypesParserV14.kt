package jp.co.soramitsu.fearless_utils.runtime.definitions.v14

import jp.co.soramitsu.fearless_utils.extensions.snakeCaseToCamelCase
import jp.co.soramitsu.fearless_utils.runtime.definitions.ParseResult
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypePreset
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypePresetBuilder
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.getOrCreate
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.newBuilder
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.TypeReference
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Alias
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.FixedArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Tuple
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Vec
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Bytes
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.Compact
import jp.co.soramitsu.fearless_utils.runtime.definitions.v14.typeMapping.SiTypeMapping
import jp.co.soramitsu.fearless_utils.runtime.definitions.v14.typeMapping.default
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.LookupSchema
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.PortableType
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.RegistryType
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefArray
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefBitSequence
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefCompact
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefComposite
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefCompositeField
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefEnum
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefSequence
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefVariant
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefVariantItem
import jp.co.soramitsu.fearless_utils.scale.EncodableStruct
import java.math.BigInteger

private const val NAME_NONE = ""

@OptIn(ExperimentalUnsignedTypes::class)
object TypesParserV14 {

    private class Params(
        val types: List<EncodableStruct<PortableType>>,
        val typeMapping: SiTypeMapping,
        val typesBuilder: TypePresetBuilder
    )

    fun parse(
        lookup: EncodableStruct<LookupSchema>,
        typePreset: TypePreset,
        typeMapping: SiTypeMapping = SiTypeMapping.default()
    ): ParseResult {
        val builder = typePreset.newBuilder()
        val params = Params(lookup[LookupSchema.types], typeMapping, builder)

        parseParams(params)

        val unknownTypes = params.typesBuilder.entries
            .mapNotNull { (name, typeRef) -> if (!typeRef.isResolved()) name else null }

        return ParseResult(params.typesBuilder, unknownTypes)
    }

    private fun parseParams(params: Params) {
        for (type in params.types) {
            val t = parseParam(params, type) ?: continue
            params.typesBuilder.type(t)
        }
    }

    private fun parseParam(params: Params, portableType: EncodableStruct<PortableType>): Type<*>? {
        val typesBuilder = params.typesBuilder
        val name = portableType[PortableType.id].toString()
        val type = portableType[PortableType.type]
        val def = type[RegistryType.def]

        val fromTypeMapping = params.typeMapping.map(type, name, params.typesBuilder)

        if (fromTypeMapping != null) {
            return fromTypeMapping
        }

        return when (def) {
            is EncodableStruct<*> -> {
                when (def.schema) {
                    is TypeDefComposite -> {
                        val list = def[TypeDefComposite.fields2]
                        val children = parseTypeMapping(params, list, useSnakeCaseForFieldNames = true)

                        Struct(name, children)
                    }

                    is TypeDefArray -> {
                        FixedArray(
                            name,
                            def[TypeDefArray.len].toInt(),
                            params.typesBuilder.getOrCreate(def[TypeDefArray.type].toString())
                        )
                    }

                    is TypeDefSequence -> {
                        Vec(
                            name,
                            params.typesBuilder.getOrCreate(def[TypeDefSequence.type].toString())
                        )
                    }

                    is TypeDefVariant -> {
                        val variants = def[TypeDefVariant.variants]

                        val transformedVariants = variants.map {
                            val fields = it[TypeDefVariantItem.fields2]

                            val itemName = it[TypeDefVariantItem.index].toString()

                            val children = parseTypeMapping(params, fields, useSnakeCaseForFieldNames = false)

                            if (children.size == 1) {
                                val (childName, childTypeRef) = children.entries.first()

                                if (childName == NAME_NONE) {
                                    return@map DictEnum.Entry(
                                        name = it[TypeDefVariantItem.name],
                                        value = childTypeRef
                                    )
                                }
                            }

                            val struct = Struct(itemName, children)

                            DictEnum.Entry(
                                name = it[TypeDefVariantItem.name],
                                value = TypeReference(struct)
                            )
                        }

                        DictEnum(name, transformedVariants)
                    }

                    is TypeDefCompact -> Compact(name)

                    is TypeDefBitSequence -> {
                        Tuple(
                            name = name,
                            typeReferences = listOf(
                                def[TypeDefBitSequence.bit_store_type],
                                def[TypeDefBitSequence.bit_order_type]
                            ).map { params.typesBuilder.getOrCreate(it.toString()) }
                        )
                    }
                    else -> null
                }
            }
            is TypeDefEnum -> {
                when (def) {
                    TypeDefEnum.str -> {
                        Alias(name, TypeReference(Bytes))
                    }
                    TypeDefEnum.char -> {
                        Alias(name, TypeReference(Bytes))
                    }
                    else -> {
                        Alias(name, typesBuilder.getOrCreate(def.localName))
                    }
                }
            }
            is List<*> -> {
                (def as? List<BigInteger>)?.let { list ->
                    Tuple(
                        name,
                        list.map { params.typesBuilder.getOrCreate(it.toString()) }
                    )
                }
            }
            else -> {
                null
            }
        }
    }

    private fun parseTypeMapping(
        params: Params,
        childrenRaw: List<EncodableStruct<TypeDefCompositeField>>,
        useSnakeCaseForFieldNames: Boolean
    ): LinkedHashMap<String, TypeReference> {
        val children = LinkedHashMap<String, TypeReference>()

        for (child in childrenRaw) {
            val typeIndex = child[TypeDefCompositeField.type].toString()

            val entryName = child[TypeDefCompositeField.name] ?: NAME_NONE

            val entryNameTransformed = if (useSnakeCaseForFieldNames) {
                entryName.snakeCaseToCamelCase()
            } else {
                entryName
            }

            children[entryNameTransformed] = params.typesBuilder.getOrCreate(typeIndex)
        }

        return children
    }
}
