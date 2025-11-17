package io.novasama.substrate_sdk_android.koltinx_serialization_scale

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.decoder.PrimitiveDecoder
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder.RootEncoder
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers.BigIntegerSerializer
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers.ByteArrayDynamicStructSerializer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.serializer
import java.math.BigInteger
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface DynamicStructureFormat : SerialFormat {

    fun <T> encode(serializer: SerializationStrategy<T>, value: T): Any?

    fun <T> decode(deserializer: DeserializationStrategy<T>, dynamicStructure: Any?): T
}

inline fun <reified T> DynamicStructureFormat.encode(value: T): Any? {
    return encode(typeOf<T>(), value)
}

fun <T> DynamicStructureFormat.encode(type: KType, value: T): Any? {
    return encode(serializersModule.modifiedSerializer(type), value)
}

inline fun <reified T> DynamicStructureFormat.decode(dynamicStructure: Any?): T {
    return decode(typeOf<T>(), dynamicStructure)
}

fun <T> DynamicStructureFormat.decode(type: KType, dynamicStructure: Any?): T {
    return decode(serializersModule.modifiedSerializer(type), dynamicStructure)
}

@Suppress("UNCHECKED_CAST")
private fun <T> SerializersModule.modifiedSerializer(kType: KType): KSerializer<T> {
    return when {
        // We need to overwrite built-in serializer for ByteArray
        kType == typeOf<ByteArray>() -> ByteArrayDynamicStructSerializer as KSerializer<T>
        else -> serializer(kType) as KSerializer<T>
    }
}

@OptIn(ExperimentalSerializationApi::class)
open class Scale(
    serializersModules: SerializersModule
) : DynamicStructureFormat {

    override val serializersModule: SerializersModule = defaultSerializers + serializersModules

    companion object Default : Scale(EmptySerializersModule)

    override fun <T> encode(serializer: SerializationStrategy<T>, value: T): Any? {
        val encoder = RootEncoder(serializersModule)
        encoder.encodeSerializableValue(serializer, value)
        return encoder.getCurrent()
    }

    override fun <T> decode(deserializer: DeserializationStrategy<T>, dynamicStructure: Any?): T {
        val decoder = PrimitiveDecoder(serializersModule, dynamicStructure)
        return decoder.decodeSerializableValue(deserializer)
    }
}

private val defaultSerializers = SerializersModule {
    contextual(BigInteger::class, BigIntegerSerializer)
    contextual(ByteArray::class, ByteArrayDynamicStructSerializer)
}
