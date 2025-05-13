@file:OptIn(ExperimentalSerializationApi::class)

package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder

import io.novasama.substrate_sdk_android.extensions.camelCaseToSnakeCase
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.annotations.TransientStruct
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.NotSet
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.requireValueNotSet
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.requireValueSet
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule

class StructEncoder(
    override val serializersModule: SerializersModule,
    nodeConsumer: (Any?) -> Unit
) : BaseCompositeEncoder(nodeConsumer) {

    private var current = mutableMapOf<String, Any?>()

    override fun encodeIdentity(descriptor: SerialDescriptor, index: Int, value: Any?) {
        val tag = descriptor.getElementName(index).camelCaseToSnakeCase()
        current[tag] = value
    }

    override fun getEncodedValue(): Struct.Instance {
        return Struct.Instance(current)
    }
}

/**
 * Encodes struct with a single value without wrapping it into [Struct.Instance]
 * Used in for single-field associated values of enums or when [TransientStruct] is used
 */
class TransientStructEncoder(
    override val serializersModule: SerializersModule,
    nodeConsumer: (Any?) -> Unit
) : BaseCompositeEncoder(nodeConsumer) {

    private var current: Any? = NotSet

    override fun encodeIdentity(descriptor: SerialDescriptor, index: Int, value: Any?) {
        requireValueNotSet(current)
        current = value
    }

    override fun getEncodedValue(): Any? {
        return requireValueSet(current)
    }
}


class StructAsTupleEncoder(
    override val serializersModule: SerializersModule,
    nodeConsumer: (Any?) -> Unit
) : BaseCompositeEncoder(nodeConsumer) {

    private var current = mutableListOf<Any?>()

    override fun encodeIdentity(descriptor: SerialDescriptor, index: Int, value: Any?) {
        current.add(index, value)
    }

    override fun getEncodedValue(): List<*> {
        return current
    }
}
