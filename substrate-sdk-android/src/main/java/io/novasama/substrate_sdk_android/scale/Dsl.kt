@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package io.novasama.substrate_sdk_android.scale

import io.novasama.substrate_sdk_android.scale.dataType.DataType
import io.novasama.substrate_sdk_android.scale.dataType.EnumType
import io.novasama.substrate_sdk_android.scale.dataType.boolean
import io.novasama.substrate_sdk_android.scale.dataType.byte
import io.novasama.substrate_sdk_android.scale.dataType.byteArray
import io.novasama.substrate_sdk_android.scale.dataType.byteArraySized
import io.novasama.substrate_sdk_android.scale.dataType.compactInt
import io.novasama.substrate_sdk_android.scale.dataType.list
import io.novasama.substrate_sdk_android.scale.dataType.long
import io.novasama.substrate_sdk_android.scale.dataType.scalable
import io.novasama.substrate_sdk_android.scale.dataType.string
import io.novasama.substrate_sdk_android.scale.dataType.tuple
import io.novasama.substrate_sdk_android.scale.dataType.uint128
import io.novasama.substrate_sdk_android.scale.dataType.uint16
import io.novasama.substrate_sdk_android.scale.dataType.uint32
import io.novasama.substrate_sdk_android.scale.dataType.uint64
import io.novasama.substrate_sdk_android.scale.dataType.uint8
import io.novasama.substrate_sdk_android.scale.dataType.union
import java.math.BigInteger
import kotlin.reflect.KClass

typealias StructBuilder<SCHEMA> = (EncodableStruct<SCHEMA>) -> Unit

operator fun <S : Schema<S>> S.invoke(block: StructBuilder<S>? = null): EncodableStruct<S> {
    val struct = EncodableStruct(this)

    block?.invoke(struct)

    return struct
}

fun <S : Schema<S>> S.string(default: String? = null) = NonNullFieldDelegate(string, this, default)

fun <S : Schema<S>> S.uint8(default: UByte? = null) = NonNullFieldDelegate(uint8, this, default)

fun <S : Schema<S>> S.uint32(default: UInt? = null) = NonNullFieldDelegate(uint32, this, default)

fun <S : Schema<S>> S.uint16(default: Int? = null) = NonNullFieldDelegate(uint16, this, default)

fun <S : Schema<S>> S.uint128(default: BigInteger? = null) = NonNullFieldDelegate(uint128, this, default)

fun <S : Schema<S>> S.bool(default: Boolean? = null) = NonNullFieldDelegate(boolean, this, default)

fun <S : Schema<S>> S.uint64(default: BigInteger? = null) = NonNullFieldDelegate(uint64, this, default)

fun <S : Schema<S>, T : Schema<T>> S.schema(schema: T, default: EncodableStruct<T>? = null) =
    NonNullFieldDelegate(scalable(schema), this, default)

fun <S : Schema<S>, T, D : DataType<T>> S.vector(
    type: D,
    default: List<T>? = null
) = NonNullFieldDelegate(list(type), this, default)

fun <S : Schema<S>, T : Schema<T>> S.vector(
    schema: T,
    default: List<EncodableStruct<T>>? = null
) = NonNullFieldDelegate(list(scalable(schema)), this, default)

fun <S : Schema<S>> S.byte(default: Byte? = null) = NonNullFieldDelegate(byte, this, default)

fun <S : Schema<S>> S.compactInt(default: BigInteger? = null) = NonNullFieldDelegate(compactInt, this, default)

fun <S : Schema<S>> S.sizedByteArray(length: Int, default: ByteArray? = null): NonNullFieldDelegate<S, ByteArray> {
    if (default != null) {
        require(length == default.size)
    }

    return NonNullFieldDelegate(byteArraySized(length), this, default)
}

fun <S : Schema<S>, A, B> S.pair(
    first: DataType<A>,
    second: DataType<B>,
    default: Pair<A, B>? = null
) = NonNullFieldDelegate(tuple(first, second), this, default)

fun <S : Schema<S>> S.byteArray(default: ByteArray? = null): NonNullFieldDelegate<S, ByteArray> {
    return NonNullFieldDelegate(byteArray, this, default)
}

fun <S : Schema<S>> S.long(default: Long? = null) = NonNullFieldDelegate(long, this, default)

fun <S : Schema<S>> S.enum(vararg types: DataType<*>, default: Any? = null) = NonNullFieldDelegate(union(types), this, default)

fun <S : Schema<S>, E : Enum<E>> S.enum(enumClass: KClass<E>, default: E? = null) = NonNullFieldDelegate(EnumType(enumClass.java), this, default)

fun <S : Schema<S>, T> S.custom(type: DataType<T>, default: T? = null) = NonNullFieldDelegate(type, this, default)
