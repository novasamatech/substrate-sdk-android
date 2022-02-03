package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale

import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.decoding.SingleValueDecoder
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encoding.SingleValueEncoder
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.serializers.BigIntegerSerializer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.serializer
import java.math.BigInteger
import kotlin.reflect.KType

interface DynamicStructureFormat : SerialFormat {

    fun <T> encodeToDynamicStructure(serializer: SerializationStrategy<T>, value: T): Any?

    fun <T> decodeFromDynamicStructure(deserializer: DeserializationStrategy<T>, dynamicStructure: Any?): T
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> DynamicStructureFormat.encodeToDynamicStructure(value: T): Any? =
    encodeToDynamicStructure(serializersModule.serializer(), value)

@OptIn(ExperimentalSerializationApi::class)
fun <T> DynamicStructureFormat.encodeToDynamicStructure(type: KType, value: T?): Any? = if (value == null) {
    null
} else {
    encodeToDynamicStructure(serializersModule.serializer(type), value)
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> DynamicStructureFormat.decodeFromDynamicStructure(dynamicStructure: Any?): T =
    decodeFromDynamicStructure(serializersModule.serializer(), dynamicStructure)

private val defaultSerializers = SerializersModule {
    contextual(BigInteger::class, BigIntegerSerializer)
}

@OptIn(ExperimentalSerializationApi::class)
open class Scale(
    serializersModules: SerializersModule
) : DynamicStructureFormat {
    override val serializersModule: SerializersModule = defaultSerializers + serializersModules

    companion object Default : Scale(EmptySerializersModule)

    override fun <T> encodeToDynamicStructure(serializer: SerializationStrategy<T>, value: T): Any? {
        val encoder = SingleValueEncoder(serializersModule)
        encoder.encodeSerializableValue(serializer, value)

        return encoder.result()
    }

    override fun <T> decodeFromDynamicStructure(deserializer: DeserializationStrategy<T>, dynamicStructure: Any?): T {
        val decoder = SingleValueDecoder(serializersModule, dynamicStructure)

        return decoder.decodeSerializableValue(deserializer)
    }
}
