@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decoder

import io.novasama.substrate_sdk_android.extensions.camelCaseToSnakeCase
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
        val key = descriptor.getElementName(index).camelCaseToSnakeCase()
        return value[key]
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
 * Decode a transient struct - a single field "struct" that is not wrapped
 * with Struct.Instance but rather represented by the value directly
 * Used in Enums
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
