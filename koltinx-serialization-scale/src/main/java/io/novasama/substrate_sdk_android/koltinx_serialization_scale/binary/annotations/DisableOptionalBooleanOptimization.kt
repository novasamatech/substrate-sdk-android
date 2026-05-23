@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Disables the SCALE `Option<bool>` single-byte optimization for the annotated `Boolean?` property.
 * When present, the property is encoded using the standard 2-byte optional encoding:
 * `0x00` for null, `0x01 0x00` for false, `0x01 0x01` for true.
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class DisableOptionalBooleanOptimization
