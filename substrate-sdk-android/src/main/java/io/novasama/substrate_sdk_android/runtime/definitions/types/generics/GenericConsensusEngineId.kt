package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypePresetBuilder
import io.novasama.substrate_sdk_android.runtime.definitions.registry.getOrCreate
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Vec
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.FixedByteArray

val GenericConsensusEngineId = FixedByteArray("GenericConsensusEngineId", 4)

@Suppress("FunctionName")
fun GenericConsensus(typePresetBuilder: TypePresetBuilder) = Struct(
    name = "GenericConsensus",
    mapping = linkedMapOf(
        "engine" to typePresetBuilder.getOrCreate("ConsensusEngineId"),
        "data" to TypeReference(
            Vec(
                name = "Vec<u8>",
                typeReference = typePresetBuilder.getOrCreate("u8")
            )
        )
    )
)
