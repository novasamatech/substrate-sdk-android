package io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Instruct serializer to encode given regular enum as dict enum entry with null associated value
 *
 * Example:
 * ```
 * @Serializable
 * @AsDictEnum
 * enum class TestEnumDict {
 *     A, B
 * }
 *
 * val decoded = TestEnumDict.A
 * val encoded = DictEnum.Entry("A", null)
 *
 * assert(decoded == Scale.decode<TestEnumDict>(encoded))
 * assert(encoded == Scale.encode(decoded))
 * ```
 **/
@OptIn(ExperimentalSerializationApi::class)
@Target(AnnotationTarget.CLASS)
@SerialInfo
annotation class AsDictEnum