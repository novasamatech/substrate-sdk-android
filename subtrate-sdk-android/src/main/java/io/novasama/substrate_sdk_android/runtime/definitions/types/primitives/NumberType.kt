package io.novasama.substrate_sdk_android.runtime.definitions.types.primitives

import java.math.BigInteger

abstract class NumberType(name: String) : Primitive<BigInteger>(name) {

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is BigInteger
    }
}
