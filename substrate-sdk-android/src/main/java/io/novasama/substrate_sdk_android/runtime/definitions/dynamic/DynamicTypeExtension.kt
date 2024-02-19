package io.novasama.substrate_sdk_android.runtime.definitions.dynamic

import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference

typealias TypeProvider = (typeDef: String) -> TypeReference

interface DynamicTypeExtension {

    fun createType(name: String, typeDef: String, typeProvider: TypeProvider): Type<*>?
}
