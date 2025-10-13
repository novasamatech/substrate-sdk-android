package io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Instruct serializer to encode given regular enum as dict enum
 *
 * Example:
 * ```
 * @AsDictEnum
 * @Serializable
 * data enum AsDictEnum(val field1: Boolean, val field2: Boolean)
 *
 * val decoded = AsTupleStruct(true, false)
 * val encoded = listOf(true, false)
 *
 * assert(decoded == Scale.decode<AsTupleStruct>(encoded))
 * assert(encoded == Scale.encode(decoded))
 * ```
 **/
@OptIn(ExperimentalSerializationApi::class)
@Target(AnnotationTarget.CLASS)
@SerialInfo
annotation class AsDictEnum