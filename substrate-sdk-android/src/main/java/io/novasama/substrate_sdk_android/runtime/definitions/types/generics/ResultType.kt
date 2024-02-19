package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum

class ResultType(ok: TypeReference, err: TypeReference) : DictEnum(
    "Result",
    listOf(
        Entry(Ok, ok),
        Entry(Err, err)
    )
) {

    companion object {
        const val Ok = "Ok"
        const val Err = "Err"
    }
}
