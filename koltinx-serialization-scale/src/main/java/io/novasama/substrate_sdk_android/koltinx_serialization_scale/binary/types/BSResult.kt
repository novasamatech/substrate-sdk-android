package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.types

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.EnumIndex
import kotlinx.serialization.Serializable

/**
 * Sealed class representing a result which can either be a successful case with a value or an error case with an error.
 *
 * This class is typically used to handle operations that can result in two distinct outcomes: success (`Ok`) or
 * failure (`Err`), along with their associated data.
 *
 * @param T The type of the value in the successful case (`Ok`).
 * @param E The type of the error in the error case (`Err`).
 */
@Serializable
sealed class BSResult<out T, out E> {

    @Serializable
    @EnumIndex(0)
    data class Ok<T>(val value: T) : BSResult<T, Nothing>()

    @Serializable
    @EnumIndex(1)
    data class Err<E>(val error: E) : BSResult<Nothing, E>()
}

@Suppress("UNCHECKED_CAST")
fun <T, E> BSResult<T, E>.toKotlinResult(): Result<T>  {
    return when(this) {
        is BSResult.Err<*> -> Result.failure(ScaleResultException(error))
        is BSResult.Ok<*> -> Result.success(value as T)
    }
}

private class ScaleResultException(error: Any?) : Exception() {
    override val message: String? = error.toString()
}