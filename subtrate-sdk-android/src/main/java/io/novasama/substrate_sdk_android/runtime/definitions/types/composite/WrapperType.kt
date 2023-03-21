package io.novasama.substrate_sdk_android.runtime.definitions.types.composite

import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.skipAliasesOrNull

abstract class WrapperType<I>(name: String, val typeReference: TypeReference) : Type<I>(name) {

    val innerType: Type<*>?
        get() = typeReference.value

    override val isFullyResolved: Boolean
        get() = typeReference.isResolved()

    inline fun <reified R> innerType(): R? {
        return typeReference.skipAliasesOrNull()?.value as? R?
    }
}
