@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decoder.PrimitiveBinaryScaleDecoder
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.encoder.PrimitiveBinaryScaleEncoder
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.serializers.BigIntegerBinarySerializer
import io.novasama.substrate_sdk_android.runtime.definitions.types.useScaleWriter
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.serializer
import java.math.BigInteger
import kotlin.reflect.KType
import kotlin.reflect.typeOf

inline fun <reified T> BinaryScale.decodeFromByteArray(bytes: ByteArray): T {
    return decodeFromByteArray(typeOf<T>(), bytes)
}

inline fun <reified T> BinaryScale.encodeToByteArray(value: T): ByteArray {
    return encodeToByteArray(typeOf<T>(), value)
}

@Suppress("UNCHECKED_CAST")
fun <T> BinaryScale.decodeFromByteArray(type: KType, bytes: ByteArray): T {
    return decodeFromByteArray(serializersModule.serializer(type) as KSerializer<T>, bytes)
}

@Suppress("UNCHECKED_CAST")
fun <T> BinaryScale.encodeToByteArray(type: KType, value: T): ByteArray {
    return encodeToByteArray(serializersModule.serializer(type) as KSerializer<T>, value)
}

open class BinaryScale(
    serializersModules: SerializersModule
) : BinaryFormat {

    override val serializersModule: SerializersModule = defaultSerializers + serializersModules

    override fun <T> decodeFromByteArray(
        deserializer: DeserializationStrategy<T>,
        bytes: ByteArray
    ): T {
        val scaleReader = ScaleCodecReader(bytes)
        val decoder = PrimitiveBinaryScaleDecoder(serializersModule, scaleReader, null, null)
        return decoder.decodeSerializableValue(deserializer)
    }

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        return useScaleWriter {
            val encoder = PrimitiveBinaryScaleEncoder(serializersModule, this@useScaleWriter, null)
            encoder.encodeSerializableValue(serializer, value)
        }
    }

    companion object Default : BinaryScale(EmptySerializersModule())
}

private val defaultSerializers = SerializersModule {
    contextual(BigInteger::class, BigIntegerBinarySerializer)
}
