package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale

import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

interface DynamicStructureFormat: SerialFormat {

    fun <T> encodeToDynamicStructure(serializer: SerializationStrategy<T>, value: T): Any?

    fun <T> decodeFromDynamicStructure(deserializer: DeserializationStrategy<T>, dynamicStructure: Any?): T
}

@OptIn(ExperimentalSerializationApi::class)
public inline fun <reified T> DynamicStructureFormat.encodeToDynamicStructure(value: T): Any? =
    encodeToDynamicStructure(serializersModule.serializer(), value)

@OptIn(ExperimentalSerializationApi::class)
public inline fun <reified T> DynamicStructureFormat.decodeFromDynamicStructure(dynamicStructure: Any?): T =
    decodeFromDynamicStructure(serializersModule.serializer(), dynamicStructure)


@OptIn(ExperimentalSerializationApi::class)
sealed class Scale(
    override val serializersModule: SerializersModule
) : DynamicStructureFormat {

    companion object Default : Scale(EmptySerializersModule)

    override fun <T> encodeToDynamicStructure(serializer: SerializationStrategy<T>, value: T): Any? {
        TODO("Not yet implemented")
    }

    override fun <T> decodeFromDynamicStructure(deserializer: DeserializationStrategy<T>, dynamicStructure: Any?): T {
        TODO("Not yet implemented")
    }
}


