@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.Serializable

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class FixedLength(val length: Int)

// Cannot be value class as FixedLength info in descriptor is lost in this case
@Serializable
data class WithLength20<T>(@FixedLength(20) val value: T)

@Serializable
data class WithLength32<T>(@FixedLength(32) val value: T)

@Serializable
data class WithLength64<T>(@FixedLength(64) val value: T)
