package io.novasama.substrate_sdk_android.runtime.metadata.module

import io.novasama.substrate_sdk_android.runtime.definitions.types.Type

class RuntimeApi(
    val name: String,
    val methods: List<RuntimeApiMethod>
)

class RuntimeApiMethod(
    val apiName: String,
    val name: String,
    val inputs: List<RuntimeApiMethodParam>,
    val output: Type<*>?,
)

class RuntimeApiMethodParam(
    val name: String,
    val type: Type<*>?,
)