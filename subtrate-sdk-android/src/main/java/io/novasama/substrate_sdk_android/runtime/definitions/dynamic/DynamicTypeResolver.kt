package io.novasama.substrate_sdk_android.runtime.definitions.dynamic

import io.novasama.substrate_sdk_android.extensions.tryFindNonNull
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.extentsions.BoxExtension
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.extentsions.CompactExtension
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.extentsions.FixedArrayExtension
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.extentsions.HashMapExtension
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.extentsions.OptionExtension
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.extentsions.ResultTypeExtension
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.extentsions.TupleExtension
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.extentsions.VectorExtension
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type

class DynamicTypeResolver(
    val extensions: List<DynamicTypeExtension>
) {
    constructor(vararg extensions: DynamicTypeExtension) : this(extensions.toList())

    companion object {
        fun defaultCompoundResolver(): DynamicTypeResolver {
            return DynamicTypeResolver(DEFAULT_COMPOUND_EXTENSIONS)
        }

        val DEFAULT_COMPOUND_EXTENSIONS = listOf(
            VectorExtension,
            CompactExtension,
            OptionExtension,
            BoxExtension,
            TupleExtension,
            FixedArrayExtension,
            HashMapExtension,
            ResultTypeExtension
        )
    }

    fun createDynamicType(
        name: String,
        typeDef: String,
        innerTypeProvider: TypeProvider
    ): Type<*>? {
        return extensions.tryFindNonNull { it.createType(name, typeDef, innerTypeProvider) }
    }
}
