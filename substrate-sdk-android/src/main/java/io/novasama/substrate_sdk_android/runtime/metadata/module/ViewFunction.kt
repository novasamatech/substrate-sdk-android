package io.novasama.substrate_sdk_android.runtime.metadata.module

import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.metadata.WithName

class ViewFunction(
    override val name: String,
    val id: ByteArray,
    val palletName: String,
    val inputs: List<RuntimeApiMethodParam>,
    val output: Type<*>?,
) : WithName
