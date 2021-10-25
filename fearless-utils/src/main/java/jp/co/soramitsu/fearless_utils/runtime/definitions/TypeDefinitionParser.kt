package jp.co.soramitsu.fearless_utils.runtime.definitions

import com.google.gson.annotations.SerializedName
import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.DynamicTypeResolver
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypePreset
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypePresetBuilder
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.create
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.getOrCreate
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.newBuilder
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.TypeReference
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Alias
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.CollectionEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.SetType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import java.math.BigInteger

class TypeDefinitionsTree(
    @SerializedName("runtime_id")
    val runtimeId: Int?,
    val types: Map<String, Any>,
    val versioning: List<Versioning>?
) {

    class Versioning(
        @SerializedName("runtime_range") val range: List<Int?>,
        val types: Map<String, Any>
    ) {
        val from: Int
            get() = range.first()!!
    }
}

class ParseResult(
    val typePreset: TypePreset,
    val unknownTypes: List<String>
)

private const val TOKEN_SET = "set"
private const val TOKEN_STRUCT = "struct"
private const val TOKEN_ENUM = "enum"

object TypeDefinitionParser {

    private class Params(
        val types: Map<String, Any>,
        val dynamicTypeResolver: DynamicTypeResolver,
        val typesBuilder: TypePresetBuilder
    )

    fun parseBaseDefinitions(
        tree: TypeDefinitionsTree,
        typePreset: TypePreset,
        dynamicTypeResolver: DynamicTypeResolver = DynamicTypeResolver.defaultCompoundResolver()
    ): ParseResult {
        val builder = typePreset.newBuilder()

        val params = Params(tree.types, dynamicTypeResolver, builder)

        parseTypes(params)

        val unknownTypes = params.typesBuilder.entries
            .mapNotNull { (name, typeRef) -> if (!typeRef.isResolved()) name else null }

        return ParseResult(params.typesBuilder, unknownTypes)
    }

    fun parseNetworkVersioning(
        tree: TypeDefinitionsTree,
        typePreset: TypePreset,
        currentRuntimeVersion: Int = tree.runtimeId!!,
        dynamicTypeResolver: DynamicTypeResolver = DynamicTypeResolver.defaultCompoundResolver()
    ): ParseResult {
        val versioning = tree.versioning
        requireNotNull(versioning)

        val builder = typePreset.newBuilder()

        versioning.filter { it.from <= currentRuntimeVersion }
            .sortedBy(TypeDefinitionsTree.Versioning::from)
            .forEach {
                parseTypes(Params(it.types, dynamicTypeResolver, builder))
            }

        val unknownTypes = builder.entries
            .mapNotNull { (name, typeRef) -> if (!typeRef.isResolved()) name else null }

        return ParseResult(builder, unknownTypes)
    }

    private fun parseTypes(parsingParams: Params) {
        for (name in parsingParams.types.keys) {
            val type = parse(parsingParams, name) ?: continue

            parsingParams.typesBuilder.type(type)
        }
    }

    private fun parse(parsingParams: Params, name: String): Type<*>? {
        val typeValue = parsingParams.types[name]

        return parseType(parsingParams, name, typeValue)
    }

    private fun parseType(parsingParams: Params, name: String, typeValue: Any?): Type<*>? {
        val typesBuilder = parsingParams.typesBuilder

        return when (typeValue) {
            is String -> {
                val dynamicType = resolveDynamicType(parsingParams, name, typeValue)

                when {
                    dynamicType != null -> dynamicType
                    typeValue == name -> parsingParams.typesBuilder[name]?.value
                    else -> Alias(name, typesBuilder.getOrCreate(typeValue))
                }
            }

            is Map<*, *> -> {
                val typeValueCasted = typeValue as Map<String, Any?>

                when (typeValueCasted["type"]) {
                    TOKEN_STRUCT -> {
                        val typeMapping = typeValueCasted["type_mapping"] as List<List<String>>
                        val children = parseTypeMapping(parsingParams, typeMapping)

                        Struct(name, children)
                    }

                    TOKEN_ENUM -> {
                        val valueList = typeValueCasted["value_list"] as? List<String>
                        val typeMapping = typeValueCasted["type_mapping"] as? List<List<String>>

                        when {
                            valueList != null -> CollectionEnum(name, valueList)

                            typeMapping != null -> {
                                val children = parseTypeMapping(parsingParams, typeMapping)
                                    .map { (name, typeRef) -> DictEnum.Entry(name, typeRef) }

                                DictEnum(name, children)
                            }
                            else -> null
                        }
                    }

                    TOKEN_SET -> {
                        val valueTypeName = typeValueCasted["value_type"] as String
                        val valueListRaw = typeValueCasted["value_list"] as Map<String, Double>

                        val valueTypeRef = resolveTypeAllWaysOrCreate(parsingParams, valueTypeName)

                        val valueList = valueListRaw.mapValues { (_, value) ->
                            BigInteger(value.toInt().toString())
                        }

                        SetType(name, valueTypeRef, LinkedHashMap(valueList))
                    }

                    else -> null
                }
            }

            else -> null
        }
    }

    private fun parseTypeMapping(
        parsingParams: Params,
        typeMapping: List<List<String>>
    ): LinkedHashMap<String, TypeReference> {
        val children = LinkedHashMap<String, TypeReference>()

        for ((fieldName, fieldType) in typeMapping) {
            children[fieldName] = resolveTypeAllWaysOrCreate(parsingParams, fieldType)
        }

        return children
    }

    private fun resolveDynamicType(
        parsingParams: Params,
        name: String,
        typeDef: String
    ): Type<*>? {
        return parsingParams.dynamicTypeResolver.createDynamicType(name, typeDef) {
            resolveTypeAllWaysOrCreate(parsingParams, it)
        }
    }

    private fun resolveTypeAllWaysOrCreate(
        parsingParams: Params,
        typeDef: String,
        name: String = typeDef
    ): TypeReference {
        return parsingParams.typesBuilder[name]
            ?: resolveDynamicType(parsingParams, name, typeDef)?.let(::TypeReference)
            ?: parsingParams.typesBuilder.create(name)
    }
}
