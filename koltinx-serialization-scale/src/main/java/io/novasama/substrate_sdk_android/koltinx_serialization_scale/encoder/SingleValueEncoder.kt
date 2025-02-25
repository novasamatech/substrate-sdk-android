package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder

import kotlinx.serialization.modules.SerializersModule

class SingleValueEncoder(override val serializersModule: SerializersModule) : BaseEncoder() {

    private var current: Any? = NOT_SET

    fun getCurrent(): Any? {
        require(current !== NOT_SET) {
            "Nothing was encoded"
        }

        return current
    }

    override fun encodeIdentity(value: Any?) {
        current = value
    }

    private object NOT_SET
}