package io.novasama.substrate_sdk_android.runtime.definitions.types.primitives

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot

object BooleanType : Primitive<Boolean>("bool") {

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): Boolean {
        return scaleCodecReader.readBoolean()
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: RuntimeSnapshot, value: Boolean) {
        scaleCodecWriter.write(ScaleCodecWriter.BOOL, value)
    }

    override fun isValidInstance(instance: Any?) = instance is Boolean
}
