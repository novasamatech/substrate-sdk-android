package io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations


/**
 * Can be used on sealed class to indicate fallback subclass that will be used when decoder encounters unknown variant:
 *
 * ```
 * @Serializable
 * sealed class MultiAddress {
 *      @Serializable
 *      class AccountId(val id: ByteArray): MultiAddress()
 *
 *      @Serializable
 *      @SerializedFallback
 *      class Unknown: MultiAddress()
 * }
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class SerializedFallback