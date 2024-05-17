package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.RuntimeType
import io.novasama.substrate_sdk_android.scale.utils.directWrite

object EncodeNoneToEmpty : RuntimeType<ByteArray?, Nothing>("EncodeNoneToEmpty") {

    override val isFullyResolved: Boolean = true

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): Nothing {
        error("EncodeNoneToEmpty does not support decoding; It is only intended to be used for encoding to be included into signed extras")
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: ByteArray?
    ) {
        // Do not write any bytes if the value is null
        // That whats makes this type non-decodable
        if (value != null) {
            scaleCodecWriter.directWrite(value)
        }
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is ByteArray?
    }
}
