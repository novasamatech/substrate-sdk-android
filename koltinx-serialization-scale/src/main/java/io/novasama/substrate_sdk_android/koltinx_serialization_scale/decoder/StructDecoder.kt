@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decoder

import io.novasama.substrate_sdk_android.extensions.camelCaseToSnakeCase
import io.novasama.substrate_sdk_android.extensions.snakeCaseToCamelCase
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder.TransientStructEncoder
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.modules.SerializersModule

class StructDecoder(
    override val serializersModule: SerializersModule,
    private val value: Struct.Instance,
) : BaseCompositeDecoder() {

    private var currentIndex = 0

    override fun decodeIdentity(descriptor: SerialDescriptor, index: Int): Any? {
        val key = descriptor.getElementName(index)

        if (key in value) return value[key]

        // TODO this is a temp solution to cater both camel and snake cases in struct fields
        // This should be addressed better to introducing some annotation to specify field naming strategy per entire struct
        // Also we should check the underlying layer to find out why some fields are not transformed to camel case
        val camelCaseKey = key.snakeCaseToCamelCase()
        val snakeCaseKey = key.camelCaseToSnakeCase()

        return when {
            camelCaseKey in value -> value[camelCaseKey]
            snakeCaseKey in value -> value[snakeCaseKey]
            else -> error("Key '$key' ('$camelCaseKey'/'$snakeCaseKey') is not found in the $value")
        }
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (currentIndex < descriptor.elementsCount) {
            currentIndex.also { currentIndex++ }
        } else {
            DECODE_DONE
        }
    }
}

/**
 * @see TransientStructEncoder
 */
class TransientStructDecoder(
    override val serializersModule: SerializersModule,
    private val singleField: Any?
) : BaseCompositeDecoder() {

    private var shouldDecode: Boolean = true

    override fun decodeIdentity(descriptor: SerialDescriptor, index: Int): Any? {
        return singleField
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (shouldDecode) {
            shouldDecode = false
            0
        } else {
            DECODE_DONE
        }
    }
}

class StructAsTupleDecoder(
    override val serializersModule: SerializersModule,
    private val value: List<*>
) : BaseCompositeDecoder() {

    private var currentIndex = 0

    override fun decodeIdentity(descriptor: SerialDescriptor, index: Int): Any? {
        return value[index]
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (currentIndex < descriptor.elementsCount) {
            currentIndex.also { currentIndex++ }
        } else {
            DECODE_DONE
        }
    }
}
