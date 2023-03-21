package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.fromByteArray
import io.novasama.substrate_sdk_android.runtime.definitions.types.toByteArray

object OpaqueCall : Type<GenericCall.Instance>("OpaqueCall") {

    override val isFullyResolved = true

    override fun decode(
        scaleCodecReader: ScaleCodecReader,
        runtime: RuntimeSnapshot
    ): GenericCall.Instance {
        val bytes = Bytes.decode(scaleCodecReader, runtime)

        return GenericCall.fromByteArray(runtime, bytes)
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: GenericCall.Instance
    ) {
        val callEncoded = GenericCall.toByteArray(runtime, value)

        return Bytes.encode(scaleCodecWriter, runtime, callEncoded)
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is ByteArray
    }
}
