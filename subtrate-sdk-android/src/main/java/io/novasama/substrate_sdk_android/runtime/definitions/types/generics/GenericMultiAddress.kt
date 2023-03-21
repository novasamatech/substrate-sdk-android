package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypePresetBuilder
import io.novasama.substrate_sdk_android.runtime.definitions.registry.getOrCreate
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.Compact

const val MULTI_ADDRESS_ID = "Id"

@Suppress("FunctionName")
fun GenericMultiAddress(typePresetBuilder: TypePresetBuilder) = DictEnum(
    name = "GenericMultiAddress",
    elements = listOf(
        DictEnum.Entry(MULTI_ADDRESS_ID, typePresetBuilder.getOrCreate("AccountId")),
        DictEnum.Entry("Index", TypeReference(Compact("Compact<AccountIndex>"))),
        DictEnum.Entry("Raw", typePresetBuilder.getOrCreate("Bytes")),
        DictEnum.Entry("Address32", typePresetBuilder.getOrCreate("H256")),
        DictEnum.Entry("Address20", typePresetBuilder.getOrCreate("H160"))
    )
)
