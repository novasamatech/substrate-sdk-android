package io.novasama.substrate_sdk_android.runtime.definitions.types

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.extensions.ensureExceptionType
import io.novasama.substrate_sdk_android.extensions.fromHex
import io.novasama.substrate_sdk_android.extensions.toHexString
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Alias
import io.novasama.substrate_sdk_android.runtime.definitions.types.errors.EncodeDecodeException
import java.io.ByteArrayOutputStream

/**
 * @throws CyclicAliasingException
 */
fun RuntimeType<*, *>.skipAliases(): Type<*>? {
    if (this !is Alias) return this

    return aliasedReference.skipAliasesOrNull()?.value
}

fun RuntimeType<*, *>?.isFullyResolved() = this?.isFullyResolved ?: false

/**
 * @throws EncodeDecodeException
 */
fun <D> RuntimeType<*, D>.fromByteArray(runtime: RuntimeSnapshot, byteArray: ByteArray): D {
    val reader = ScaleCodecReader(byteArray)

    return ensureUnifiedException { decode(reader, runtime) }
}

/**
 * @throws EncodeDecodeException
 */
fun <D> RuntimeType<*, D>.fromHex(runtime: RuntimeSnapshot, hex: String): D {
    return ensureUnifiedException { fromByteArray(runtime, hex.fromHex()) }
}

fun <D> RuntimeType<*, D>.fromByteArrayOrNull(runtime: RuntimeSnapshot, byteArray: ByteArray): D? {
    return runCatching { fromByteArray(runtime, byteArray) }.getOrNull()
}

fun <D> RuntimeType<*, D>.fromHexOrNull(runtime: RuntimeSnapshot, hex: String): D? {
    return runCatching { fromHex(runtime, hex) }.getOrNull()
}

/**
 * @throws EncodeDecodeException
 */
fun <E> RuntimeType<E, *>.toByteArray(runtime: RuntimeSnapshot, value: E): ByteArray {
    return ensureUnifiedException {
        useScaleWriter { encode(this, runtime, value) }
    }
}

/**
 * Type-unsafe version of [toByteArray]
 *
 * @throws EncodeDecodeException
 */
fun RuntimeType<*, *>.bytes(runtime: RuntimeSnapshot, value: Any?): ByteArray {
    return ensureUnifiedException {
        useScaleWriter { encodeUnsafe(this, runtime, value) }
    }
}

fun <E> RuntimeType<E, *>.toByteArrayOrNull(runtime: RuntimeSnapshot, value: E): ByteArray? {
    return runCatching { toByteArray(runtime, value) }.getOrNull()
}

fun RuntimeType<*, *>.bytesOrNull(runtime: RuntimeSnapshot, value: Any?): ByteArray? {
    return runCatching { bytes(runtime, value) }.getOrNull()
}

/**
 * @throws EncodeDecodeException
 */
fun <E> RuntimeType<E, *>.toHex(runtime: RuntimeSnapshot, value: E) =
    toByteArray(runtime, value).toHexString(withPrefix = true)

fun RuntimeType<*, *>.toHexUntyped(runtime: RuntimeSnapshot, value: Any?) =
    bytes(runtime, value).toHexString(withPrefix = true)

fun <E> RuntimeType<E, *>.toHexOrNull(runtime: RuntimeSnapshot, value: E) =
    toByteArrayOrNull(runtime, value)?.toHexString(withPrefix = true)

fun useScaleWriter(use: ScaleCodecWriter.() -> Unit): ByteArray {
    val stream = ByteArrayOutputStream()
    val writer = ScaleCodecWriter(stream)

    writer.use()

    return stream.toByteArray()
}

private inline fun <R> ensureUnifiedException(block: () -> R): R {
    return ensureExceptionType(::EncodeDecodeException, block)
}
