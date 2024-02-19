package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypePresetBuilder
import io.novasama.substrate_sdk_android.runtime.definitions.registry.getOrCreate
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct

@Suppress("FunctionName")
fun SessionKeysSubstrate(typePresetBuilder: TypePresetBuilder) = Struct(
    name = "SessionKeysSubstrate",
    mapping = linkedMapOf(
        "grandpa" to typePresetBuilder.getOrCreate("AccountId"),
        "babe" to typePresetBuilder.getOrCreate("AccountId"),
        "im_online" to typePresetBuilder.getOrCreate("AccountId")
    )
)
