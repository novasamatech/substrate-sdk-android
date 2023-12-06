package jp.co.soramitsu.fearless_utils.runtime.definitions.v14.typeMapping

import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypePresetBuilder
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.getOrCreate
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.TypeReference
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.PortableType
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.TypeDefComposite
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.asCompositeOrNull
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.fieldType
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.type
import jp.co.soramitsu.fearless_utils.scale.EncodableStruct

private const val DISPATCH_INFO_TYPE = "frame_support.dispatch.DispatchInfo"

class AddRuntimeDispatchInfoSiTypeMapping : SiTypeMapping {

    override fun map(
        originalDefinition: EncodableStruct<PortableType>,
        suggestedTypeName: String,
        typesBuilder: TypePresetBuilder
    ): Type<*>? {
        if (suggestedTypeName == DISPATCH_INFO_TYPE) {
            addRuntimeDispatchInfo(originalDefinition, typesBuilder)
        }

        // we don't modify any existing type
        return null
    }

    private fun addRuntimeDispatchInfo(
        dispatchInfoType: EncodableStruct<PortableType>,
        typesBuilder: TypePresetBuilder
    ) {
        val typeDef = dispatchInfoType.type.asCompositeOrNull() ?: return

        val weightType = typeDef.fieldTypeReference("weight", typesBuilder) ?: return
        val dispatchClassType = typeDef.fieldTypeReference("class", typesBuilder) ?: return
        val partialFeeType = typesBuilder.getOrCreate("u128")

        val runtimeDispatchInfo = RuntimeDispatchInfo(weightType, dispatchClassType, partialFeeType)

        typesBuilder.type(runtimeDispatchInfo)
    }

    private fun EncodableStruct<TypeDefComposite>.fieldTypeReference(
        name: String,
        typesBuilder: TypePresetBuilder
    ): TypeReference? {
        val fieldType = fieldType(name) ?: return null

        return typesBuilder.getOrCreate(fieldType.toString())
    }
}

fun RuntimeDispatchInfo(
    weightType: TypeReference,
    classType: TypeReference,
    partialFeeType: TypeReference
) = Struct(
    name = "RuntimeDispatchInfo",
    mapping = linkedMapOf(
        "weight" to weightType,
        "class" to classType,
        "partialFee" to partialFeeType
    )
)
