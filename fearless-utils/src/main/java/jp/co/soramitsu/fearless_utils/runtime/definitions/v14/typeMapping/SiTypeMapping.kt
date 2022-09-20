package jp.co.soramitsu.fearless_utils.runtime.definitions.v14.typeMapping

import jp.co.soramitsu.fearless_utils.extensions.tryFindNonNull
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypePresetBuilder
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.getOrCreate
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.TypeReference
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Alias
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Option
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.SetType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.DynamicByteArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.FixedByteArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u8
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.skipAliases
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.PortableType
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.RegistryType
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefArray
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefComposite
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefSequence
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeParameter
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.def
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.name
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.params
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.path
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.type
import jp.co.soramitsu.fearless_utils.scale.EncodableStruct
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
    return OneOfSiTypeMapping(inner + other)
}

operator fun OneOfSiTypeMapping.plus(others: List<SiTypeMapping>): OneOfSiTypeMapping {
    return OneOfSiTypeMapping(inner + others)
}

private val EncodableStruct<RegistryType>.lastPathSegment: String?
    get() = path.lastOrNull()

private val EncodableStruct<PortableType>.id: BigInteger
    get() = get(PortableType.id)

private val EncodableStruct<PortableType>.type: EncodableStruct<RegistryType>
    get() = get(PortableType.type)
