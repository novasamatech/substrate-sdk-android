package io.novasama.substrate_sdk_android.runtime.definitions.dynamic.extentsions

import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.DynamicTypeExtension
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.TypeProvider
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type

private val GENERIC_REGEX = "^([^<]*)<(.+)>\$".toRegex() // PartName<SubType>

private const val RAW_TYPE_GROUP_INDEX = 1 // first one will be the entire typeDef, the second one will be raw type

object GenericsExtension : DynamicTypeExtension {

    override fun createType(name: String, typeDef: String, typeProvider: TypeProvider): Type<*>? {
        val groups = GENERIC_REGEX.find(typeDef)?.groupValues ?: return null
        val rawType = groups.getOrNull(RAW_TYPE_GROUP_INDEX) ?: return null

        return typeProvider(rawType).value
    }
}
