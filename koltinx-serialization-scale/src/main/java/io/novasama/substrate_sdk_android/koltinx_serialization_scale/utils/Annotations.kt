@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor

inline fun <reified T: Annotation> List<Annotation>.findAnnotation(): T? {
    return find { it is T } as? T
}

inline fun <reified T> SerialDescriptor.findAnnotation(): T? {
    return annotations.findAnnotation()
}

inline fun <reified T> SerialDescriptor.isAnnotatedWith(): Boolean {
    return annotations.any { it is T }
}
