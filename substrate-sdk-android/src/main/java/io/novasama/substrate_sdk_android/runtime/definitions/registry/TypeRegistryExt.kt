package io.novasama.substrate_sdk_android.runtime.definitions.registry

import io.novasama.substrate_sdk_android.runtime.definitions.types.Type

fun TypeRegistry.getOrThrow(
    definition: String
): Type<*> {
    return get(definition) ?: error("Type $definition was not found.")
}
