package io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Instructs serialization process to skip wrapping annotated value in Struct.Instance
 * Only applicable to single-field classes
 * This annotation is useful to switch to value-class like behavior in case class cannot be annotated
 * as value class directly (e.g. it overrides hash-code)
 * Annotating class with more than 1 field with this field will cause an exception to be thrown
 * Example
 * ```
 * @TransientStruct
 * data class Transient(val field: Boolean)
 *
 * val decoded = Transient(true)
 * val encoded = true
 *
 * assert(decoded == Scale.decode<Transient>(encoded))
 * assert(encoded == Scale.encode(decoded))
 * ```
 */
@OptIn(ExperimentalSerializationApi::class)
@Target(AnnotationTarget.CLASS)
@SerialInfo
annotation class TransientStruct
