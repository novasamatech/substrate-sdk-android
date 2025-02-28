@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor

inline fun <reified T> SerialDescriptor.findAnnotation(): T? {
    return annotations.find { it is T } as? T ?: return null
}

inline fun <reified T> SerialDescriptor.isAnnotatedWith(): Boolean {
    return annotations.any { it is T }
}
