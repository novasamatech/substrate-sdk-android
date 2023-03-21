package io.novasama.substrate_sdk_android.runtime.definitions.v14.typeMapping

import io.novasama.substrate_sdk_android.extensions.tryFindNonNull
import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypePresetBuilder
import io.novasama.substrate_sdk_android.runtime.definitions.registry.getOrCreate
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Alias
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Option
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.SetType
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.DynamicByteArray
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.FixedByteArray
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u8
import io.novasama.substrate_sdk_android.runtime.definitions.types.skipAliases
import io.novasama.substrate_sdk_android.runtime.metadata.v14.PortableType
import io.novasama.substrate_sdk_android.runtime.metadata.v14.RegistryType
import io.novasama.substrate_sdk_android.runtime.metadata.v14.TypeDefArray
import io.novasama.substrate_sdk_android.runtime.metadata.v14.TypeDefComposite
import io.novasama.substrate_sdk_android.runtime.metadata.v14.TypeDefSequence
import io.novasama.substrate_sdk_android.runtime.metadata.v14.TypeParameter
import io.novasama.substrate_sdk_android.runtime.metadata.v14.def
import io.novasama.substrate_sdk_android.runtime.metadata.v14.name
import io.novasama.substrate_sdk_android.runtime.metadata.v14.params
import io.novasama.substrate_sdk_android.runtime.metadata.v14.path
import io.novasama.substrate_sdk_android.runtime.metadata.v14.type
import io.novasama.substrate_sdk_android.scale.EncodableStruct
import java.math.BigInteger

interface SiTypeMapping {

    companion object // extensions

    fun map(
        originalDefinition: EncodableStruct<PortableType>,
        suggestedTypeName: String,
        typesBuilder: TypePresetBuilder
    ): Type<*>?
}

class OneOfSiTypeMapping(
    val inner: List<SiTypeMapping>
) : SiTypeMapping {

    override fun map(
        originalDefinition: EncodableStruct<PortableType>,
        suggestedTypeName: String,
        typesBuilder: TypePresetBuilder
    ): Type<*>? {
        return inner.tryFindNonNull { it.map(originalDefinition, suggestedTypeName, typesBuilder) }
    }
}

object SiSetTypeMapping : SiTypeMapping {

    override fun map(
        originalDefinition: EncodableStruct<PortableType>,
        suggestedTypeName: String,
        typesBuilder: TypePresetBuilder,
    ): Type<*>? {
        val registryType = originalDefinition.type
        if (registryType.lastPathSegment != "BitFlags") return null

        val typeDef = registryType.def

        if (typeDef is EncodableStruct<*> && typeDef.schema is TypeDefComposite) {
            val typeIndex = typeDef[TypeDefComposite.fields2].firstOrNull()?.type

            if (typeIndex != null) {
                return SetType(
                    name = suggestedTypeName,
                    valueTypeReference = typesBuilder.getOrCreate(typeIndex.toString()),
                    valueList = LinkedHashMap()
                )
            }
        }

        return SetType(
            name = suggestedTypeName,
            valueTypeReference = TypeReference(null),
            valueList = LinkedHashMap()
        )
    }
}

object SiCompositeNoneToAliasTypeMapping : SiTypeMapping {

    override fun map(
        originalDefinition: EncodableStruct<PortableType>,
        suggestedTypeName: String,
        typesBuilder: TypePresetBuilder
    ): Type<*>? {
        val typeDef = originalDefinition.type.def

        if (
            typeDef is EncodableStruct<*> &&
            typeDef.schema is TypeDefComposite
        ) {
            val fields = typeDef[TypeDefComposite.fields2]

            if (fields.size == 1 && fields.first().name == null) {
                val aliasedTypeIndex = fields.first().type.toString()
                val aliasedReference = typesBuilder.getOrCreate(aliasedTypeIndex)

                return Alias(suggestedTypeName, aliasedReference)
            }
        }

        return null
    }
}

object SiOptionTypeMapping : SiTypeMapping {

    override fun map(
        originalDefinition: EncodableStruct<PortableType>,
        suggestedTypeName: String,
        typesBuilder: TypePresetBuilder
    ): Type<*>? {
        val path = originalDefinition.type.path

        if (path.size != 1 || path.first() != "Option") {
            return null
        }

        // always use id-based name for Options since all Options have the same apth
        val typeName = originalDefinition.id.toString()

        val innerTypeIndex = originalDefinition.type.params.firstOrNull()?.get(TypeParameter.type)
            ?: return Option(typeName, typeReference = TypeReference(null))

        return Option(typeName, typeReference = typesBuilder.getOrCreate(innerTypeIndex.toString()))
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
object SiByteArrayMapping : SiTypeMapping {

    override fun map(
        originalDefinition: EncodableStruct<PortableType>,
        suggestedTypeName: String,
        typesBuilder: TypePresetBuilder
    ): Type<*>? {
        val def = originalDefinition.type.def

        if (def is EncodableStruct<*>) {
            when (def.schema) {
                is TypeDefArray -> {
                    if (isInnerTypeU8(typesBuilder, def[TypeDefArray.type])) {
                        return FixedByteArray(
                            name = suggestedTypeName,
                            length = def[TypeDefArray.len].toInt()
                        )
                    }
                }

                is TypeDefSequence -> {
                    if (isInnerTypeU8(typesBuilder, def[TypeDefSequence.type])) {
                        return DynamicByteArray(name = suggestedTypeName)
                    }
                }
            }
        }

        return null
    }

    private fun isInnerTypeU8(typesBuilder: TypePresetBuilder, innerTypeId: BigInteger): Boolean {
        val innerType = typesBuilder[innerTypeId.toString()]

        return innerType?.skipAliases()?.value == u8
    }
}

fun SiTypeMapping.Companion.default(): OneOfSiTypeMapping {
    return OneOfSiTypeMapping(
        listOf(
            SiByteArrayMapping,
            SiOptionTypeMapping,
            SiSetTypeMapping,
            SiCompositeNoneToAliasTypeMapping
        )
    )
}

operator fun OneOfSiTypeMapping.plus(other: SiTypeMapping): OneOfSiTypeMapping {
    return OneOfSiTypeMapping(listOf(other) + inner)
}

operator fun OneOfSiTypeMapping.plus(others: List<SiTypeMapping>): OneOfSiTypeMapping {
    return OneOfSiTypeMapping(others + inner)
}

private val EncodableStruct<RegistryType>.lastPathSegment: String?
    get() = path.lastOrNull()

private val EncodableStruct<PortableType>.id: BigInteger
    get() = get(PortableType.id)

private val EncodableStruct<PortableType>.type: EncodableStruct<RegistryType>
    get() = get(PortableType.type)
