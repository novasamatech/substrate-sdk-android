package io.novasama.substrate_sdk_android.runtime.definitions.dynamic.extentsions

import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.DynamicTypeExtension
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.TypeProvider
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference

abstract class WrapperExtension : DynamicTypeExtension {

    abstract val wrapperName: String

    abstract fun createWrapper(name: String, innerTypeRef: TypeReference): Type<*>?

    override fun createType(name: String, typeDef: String, typeProvider: TypeProvider): Type<*>? {
        if (!typeDef.startsWith("$wrapperName<")) return null

        val innerTypeDef = typeDef.removeSurrounding("$wrapperName<", ">")

        val innerTypeRef = typeProvider(innerTypeDef)

        return createWrapper(name, innerTypeRef)
    }
}
