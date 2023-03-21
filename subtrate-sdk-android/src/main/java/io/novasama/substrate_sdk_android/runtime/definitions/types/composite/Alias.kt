package io.novasama.substrate_sdk_android.runtime.definitions.types.composite

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.TypeReference

class Alias(alias: String, val aliasedReference: TypeReference) : Type<Any?>(alias) {

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): Any? {
        return aliasedReference.requireValue().decode(scaleCodecReader, runtime)
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: RuntimeSnapshot, value: Any?) {
        aliasedReference.requireValue().encodeUnsafe(scaleCodecWriter, runtime, value)
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return aliasedReference.requireValue().isValidInstance(instance)
    }

    override val isFullyResolved: Boolean
        get() = aliasedReference.isResolved()
}
