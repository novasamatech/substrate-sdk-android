package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.serializers.BigIntegerSerializer
import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.serializersModuleOf
import java.math.BigInteger

interface DynamicStructureFormat : SerialFormat {

    fun <T> encodeToDynamicStructure(serializer: SerializationStrategy<T>, value: T): Any?

    fun <T> decodeFromDynamicStructure(deserializer: DeserializationStrategy<T>, dynamicStructure: Any?): T
}

@OptIn(ExperimentalSerializationApi::class)
public inline fun <reified T> DynamicStructureFormat.encodeToDynamicStructure(value: T): Any? =
    encodeToDynamicStructure(serializersModule.serializer(), value)

@OptIn(ExperimentalSerializationApi::class)
public inline fun <reified T> DynamicStructureFormat.decodeFromDynamicStructure(dynamicStructure: Any?): T =
    decodeFromDynamicStructure(serializersModule.serializer(), dynamicStructure)


private val defaultSerializers = serializersModuleOf(BigInteger::class, BigIntegerSerializer)

@OptIn(ExperimentalSerializationApi::class)
sealed class Scale(
    serializersModules: SerializersModule
) : DynamicStructureFormat {
    override val serializersModule: SerializersModule = defaultSerializers + serializersModules

    companion object Default : Scale(EmptySerializersModule)

    override fun <T> encodeToDynamicStructure(serializer: SerializationStrategy<T>, value: T): Any? {
        val encoder = RootEncoder(serializersModule)
        encoder.encodeSerializableValue(serializer, value)

        return encoder.result
    }

    override fun <T> decodeFromDynamicStructure(deserializer: DeserializationStrategy<T>, dynamicStructure: Any?): T {
        val decoder = RootDecoder(serializersModule, dynamicStructure)

        return decoder.decodeSerializableValue(deserializer)
    }
}


