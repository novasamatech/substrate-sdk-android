package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.NotSet
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.requireValueNotSet
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.requireValueSet
import kotlinx.serialization.modules.SerializersModule

open class SingleValueEncoder(override val serializersModule: SerializersModule) : BaseEncoder() {

    private var current: Any? = NotSet

    fun getCurrent(): Any? {
        return requireValueSet(current)
    }

    final override fun encodeIdentity(value: Any?) {
        requireValueNotSet(current)
        current = value
    }
}