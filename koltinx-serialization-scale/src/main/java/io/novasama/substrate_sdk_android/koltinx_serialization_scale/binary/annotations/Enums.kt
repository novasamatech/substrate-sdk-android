package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations

import kotlinx.serialization.SerialInfo

@SerialInfo
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.CLASS)
annotation class EnumIndex(val index: Int)
