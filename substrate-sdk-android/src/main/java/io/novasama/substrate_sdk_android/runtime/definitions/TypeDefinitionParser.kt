package io.novasama.substrate_sdk_android.runtime.definitions

import com.google.gson.annotations.SerializedName
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.DynamicTypeResolver
import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypePreset
import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypePresetBuilder
import io.novasama.substrate_sdk_android.runtime.definitions.registry.create
import io.novasama.substrate_sdk_android.runtime.definitions.registry.getOrCreate
import io.novasama.substrate_sdk_android.runtime.definitions.registry.newBuilder
import io.novasama.substrate_sdk_android.runtime.definitions.registry.type
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Alias
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.CollectionEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.SetType
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
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
    ): TypePreset {
        val builder = typePreset.newBuilder()

        val params = Params(tree.types, dynamicTypeResolver, builder)

        parseTypes(params)

        return params.typesBuilder
    }

    fun parseNetworkVersioning(
        tree: TypeDefinitionsTree,
        typePreset: TypePreset,
        currentRuntimeVersion: Int = tree.runtimeId!!,
        dynamicTypeResolver: DynamicTypeResolver = DynamicTypeResolver.defaultCompoundResolver()
    ): TypePreset {
        val versioning = tree.versioning
        requireNotNull(versioning)

        val builder = typePreset.newBuilder()

        versioning.filter { it.from <= currentRuntimeVersion }
            .sortedBy(TypeDefinitionsTree.Versioning::from)
            .forEach {
                parseTypes(Params(it.types, dynamicTypeResolver, builder))
            }

        return builder
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
