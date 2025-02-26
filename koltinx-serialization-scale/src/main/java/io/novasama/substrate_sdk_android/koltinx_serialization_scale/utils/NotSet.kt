package io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils

internal object NotSet

fun requireValueSet(value: Any?): Any? {
    require(value !== NotSet) {
        "Value was not set"
    }

    return value
}

fun requireValueNotSet(value: Any?) {
    require(value === NotSet) {
        "Value was already set"
    }
}