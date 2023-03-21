package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypePresetBuilder
import io.novasama.substrate_sdk_android.runtime.definitions.registry.getOrCreate
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct

@Suppress("FunctionName")
fun GenericSealV0(typePresetBuilder: TypePresetBuilder) = Struct(
    name = "GenericSealV0",
    mapping = linkedMapOf(
        "slot" to typePresetBuilder.getOrCreate("u64"),
        "signature" to typePresetBuilder.getOrCreate("Signature")
    )
)

@Suppress("FunctionName")
fun GenericSeal(typePresetBuilder: TypePresetBuilder) = Struct(
    name = "GenericSeal",
    mapping = linkedMapOf(
        "engine" to typePresetBuilder.getOrCreate("ConsensusEngineId"),
        "data" to typePresetBuilder.getOrCreate("Bytes")
    )
)
