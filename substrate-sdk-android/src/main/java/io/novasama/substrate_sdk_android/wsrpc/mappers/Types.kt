package io.novasama.substrate_sdk_android.wsrpc.mappers

import com.google.gson.Gson
import io.novasama.substrate_sdk_android.extensions.fromHex
import io.novasama.substrate_sdk_android.scale.EncodableStruct
import io.novasama.substrate_sdk_android.scale.Schema
import io.novasama.substrate_sdk_android.wsrpc.exception.RpcException
import io.novasama.substrate_sdk_android.wsrpc.response.RpcResponse

/**
 *  Mark that the result is always non-null and null result means that error happened
 * @throws RpcException in case of null result
 */
fun <R> NullableMapper<R>.nonNull() = NonNullMapper(this)

fun <S : Schema<S>> scale(schema: S) = ScaleMapper(schema)

fun <S : Schema<S>> scaleCollection(schema: S) = ScaleCollectionMapper(schema)

inline fun <reified T> pojo() = POJOMapper(T::class.java)

internal fun stringIdMapper() = StringIdMapper

inline fun <reified T> pojoList() = POJOCollectionMapper(T::class.java)

object StringIdMapper : NullableMapper<String>() {

    override fun mapNullable(rpcResponse: RpcResponse, jsonMapper: Gson): String? {
        return when (val result = rpcResponse.result) {
            is Double -> result.toLong().toString()
            else -> result?.toString()
        }
    }
}

class ScaleMapper<S : Schema<S>>(val schema: S) : NullableMapper<EncodableStruct<S>>() {
    override fun mapNullable(rpcResponse: RpcResponse, jsonMapper: Gson): EncodableStruct<S>? {
        val raw = rpcResponse.result as? String ?: return null

        return schema.read(raw.fromHex())
    }
}

class ScaleCollectionMapper<S : Schema<S>>(val schema: S) :
    NullableMapper<List<EncodableStruct<S>>>() {

    override fun mapNullable(
        rpcResponse: RpcResponse,
        jsonMapper: Gson
    ): List<EncodableStruct<S>>? {
        val raw = rpcResponse.result as? List<String> ?: return null

        return raw.map(schema::read)
    }
}

class POJOCollectionMapper<T>(val classRef: Class<T>) : NullableMapper<List<T>>() {
    override fun mapNullable(rpcResponse: RpcResponse, jsonMapper: Gson): List<T>? {
        val raw = rpcResponse.result as? List<*> ?: return null
        return raw.map {
            val t = jsonMapper.toJsonTree(it)
            jsonMapper.fromJson(t, classRef)
        }
    }
}

class POJOMapper<T>(val classRef: Class<T>) : NullableMapper<T>() {

    override fun mapNullable(rpcResponse: RpcResponse, jsonMapper: Gson): T? {
        return when (rpcResponse.result) {
            is Map<*, *> -> {
                val tree = jsonMapper.toJsonTree(rpcResponse.result)
                jsonMapper.fromJson(tree, classRef)
            }
            else -> rpcResponse.result as? T ?: null
        }
    }
}
