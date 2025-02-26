@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.descriptors.SerialDescriptor

/**
 * Can be used on sealed class to indicate fallback subclass that will be used when decoder encounters unknown enum variant
 *
 * This can be used on Dict Enums (represented by Sealed Classes):
 *
 * ```
 * @Serializable
 * @SerializedFallback("Unknown")
 * sealed class MultiAddress {
 *      @Serializable
 *      class AccountId(val id: ByteArray): MultiAddress()
 *
 *      @Serializable
 *      class Unknown: MultiAddress()
 * }
 * ```
 *
 * Or on Collection Enums (represented by regular enums):
 *
 * ```
 * @Serializable
 * @SerializedFallback("WHITE")
 * enum class Color {
 *    RED, BLUE, WHITE
 * }
 * ```
 */
@OptIn(ExperimentalSerializationApi::class)
@Target(AnnotationTarget.CLASS)
@SerialInfo
annotation class SerializedFallback(val fallback: String)

fun SerialDescriptor.findSerializedFallback(): String? {
    val fallbackAnnotation = annotations.find { it is SerializedFallback }
        as? SerializedFallback ?: return null

    return fallbackAnnotation.fallback
}
