package io.novasama.substrate_sdk_android.runtime.definitions.types.primitives

import io.novasama.substrate_sdk_android.runtime.definitions.types.Type

abstract class Primitive<I>(name: String) : Type<I>(name) {

    override val isFullyResolved = true
}
