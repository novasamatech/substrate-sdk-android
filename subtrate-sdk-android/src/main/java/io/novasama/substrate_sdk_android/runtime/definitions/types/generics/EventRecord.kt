package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypePresetBuilder
import io.novasama.substrate_sdk_android.runtime.definitions.registry.getOrCreate
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Vec

@Suppress("FunctionName")
fun EventRecord(typePresetBuilder: TypePresetBuilder) = Struct(
    name = "EventRecord",
    mapping = linkedMapOf(
        "phase" to typePresetBuilder.getOrCreate("Phase"),
        "event" to typePresetBuilder.getOrCreate("GenericEvent"),
        "topics" to TypeReference(
            Vec(
                name = "Vec<Hash>",
                typeReference = typePresetBuilder.getOrCreate("Hash")
            )
        )
    )
)
