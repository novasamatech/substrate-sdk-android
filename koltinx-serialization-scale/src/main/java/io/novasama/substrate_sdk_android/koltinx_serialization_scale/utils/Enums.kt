package io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind

fun SerialDescriptor.shouldUseTransientStructInEnum(): Boolean {
    return kind is StructureKind.CLASS && elementsCount == 1
}