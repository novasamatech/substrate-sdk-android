@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.DynamicStructureFormat
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.annotations.FixedLengthBytes
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.binary.decoder.PrimitiveBinaryScaleDecoder
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.serializers.ByteArraySerializer
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.reflect.KType
import kotlin.reflect.typeOf

inline fun <reified T> BinaryScale.decodeFromByteArray(bytes: ByteArray): T {
    return decodeFromByteArray(typeOf<T>(), bytes)
}

@Suppress("UNCHECKED_CAST")
fun <T> BinaryScale.decodeFromByteArray(type: KType, bytes: ByteArray): T {
    return decodeFromByteArray(serializersModule.serializer(type) as KSerializer<T>, bytes)
}

open class BinaryScale(
    serializersModules: SerializersModule
) : BinaryFormat {

    override val serializersModule: SerializersModule =  serializersModules

    override fun <T> decodeFromByteArray(
        deserializer: DeserializationStrategy<T>,
        bytes: ByteArray
    ): T {
        val scaleReader = ScaleCodecReader(bytes)
        val decoder = PrimitiveBinaryScaleDecoder(serializersModule, scaleReader, elementContext = null)
        return decoder.decodeSerializableValue(deserializer)
    }

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        TODO("Not yet implemented")
    }

    companion object Default : BinaryScale(EmptySerializersModule)
}
