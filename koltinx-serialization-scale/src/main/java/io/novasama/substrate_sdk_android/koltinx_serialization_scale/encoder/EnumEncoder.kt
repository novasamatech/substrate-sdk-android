package io.novasama.substrate_sdk_android.koltinx_serialization_scale.encoder

import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.NotSet
import io.novasama.substrate_sdk_android.koltinx_serialization_scale.utils.requireValueSet
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule

/**
 * This implementation is based on how [AbstractPolymorphicSerializer] encodes sealed values
 * In particular, it expects two calls to be done in a sequence:
 *
 * 1. `encodeStringElement(descriptor, 0, actualSerializer.descriptor.serialName)`
 * 2. `encodeSerializableElement(descriptor, 1, actualSerializer.cast(), value)`
 *
 * So it first encodes variant name and then the variant value
 */
class EnumEncoder(
    override val serializersModule: SerializersModule,
    nodeConsumer: (Any?) -> Unit,
) : BaseCompositeEncoder(nodeConsumer) {

    private var _variantName: String? = null
    private val variantName: String
        get() = requireNotNull(_variantName) {
            "Variant name was not encoded"
        }

    private var variantValue: Any? = NotSet

    override fun encodeIdentity(descriptor: SerialDescriptor, index: Int, value: Any?) {
        variantValue = value
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        _variantName = qualifiedClassNameToVariantName(value)
    }

    override fun getEncodedValue(): Any {
        return DictEnum.Entry(variantName, requireValueSet(variantValue))
    }

    override fun createSerializableElementEncoder(): SingleValueEncoder {
        return EnumInnerTransientStructEncoder(serializersModule)
    }

    private class EnumInnerTransientStructEncoder(serializersModule: SerializersModule) :
        SingleValueEncoder(serializersModule) {

        override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
            if (descriptor.shouldUseTransientStructEncoding()) {
                return TransientStructEncoder(serializersModule, nodeConsumer = ::encodeIdentity)
            }

            return super.beginStructure(descriptor)
        }

        private fun SerialDescriptor.shouldUseTransientStructEncoding(): Boolean {
            return kind is StructureKind.CLASS && elementsCount == 1
        }
    }

    private fun qualifiedClassNameToVariantName(qualified: String): String {
        return qualified.split(".").last()
    }
}