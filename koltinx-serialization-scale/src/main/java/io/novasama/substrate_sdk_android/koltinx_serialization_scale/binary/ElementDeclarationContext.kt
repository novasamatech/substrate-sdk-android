@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.findAnnotation
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor

class ElementDeclarationContext(
    val index: Int,
    val descriptor: SerialDescriptor,
)

val ElementDeclarationContext.elementAnnotations: List<Annotation>
    get() = descriptor.getElementAnnotations(index)

inline fun <reified T : Annotation> ElementDeclarationContext.findElementAnnotation(): T? {
    return elementAnnotations.findAnnotation()
}

inline fun <reified T : Annotation> ElementDeclarationContext.hasAnnotation(): Boolean {
    return findElementAnnotation<T>() != null
}
