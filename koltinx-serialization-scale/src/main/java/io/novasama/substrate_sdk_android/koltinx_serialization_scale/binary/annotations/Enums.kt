package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations

import kotlinx.serialization.SerialInfo

@SerialInfo
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class EnumIndex(val index: Byte)