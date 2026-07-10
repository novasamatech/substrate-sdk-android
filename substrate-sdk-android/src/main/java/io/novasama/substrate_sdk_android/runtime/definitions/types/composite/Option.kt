package io.novasama.substrate_sdk_android.runtime.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference
import io.novasama.substrate_sdk_android.runtime.definitions.types.errors.EncodeDecodeException
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.BooleanType

class Option(
    name: String,
    typeReference: TypeReference
) : WrapperType<Any?>(name, typeReference) {

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): Any? {
        if (typeReference.requireValue() is BooleanType) {
            return when (scaleCodecReader.readByte().toInt()) {
                0 -> null
                1 -> true
                2 -> false
                else -> throw EncodeDecodeException("Not a optional boolean")
            }
        }

        val some: Boolean = scaleCodecReader.readBoolean()

        return if (some) typeReference.requireValue().decode(scaleCodecReader, runtime) else null
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: RuntimeSnapshot, value: Any?) {
        val type = typeReference.requireValue()
        if (type is BooleanType) {
            return when (value as Boolean?) {
                null -> scaleCodecWriter.writeByte(0)
                true -> scaleCodecWriter.writeByte(1)
                false -> scaleCodecWriter.writeByte(2)
            }
        }

        if (value == null) {
            scaleCodecWriter.write(ScaleCodecWriter.BOOL, false)
        } else {
            scaleCodecWriter.write(ScaleCodecWriter.BOOL, true)
            type.encodeUnsafe(scaleCodecWriter, runtime, value)
        }
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance == null || typeReference.requireValue().isValidInstance(instance)
    }
}
