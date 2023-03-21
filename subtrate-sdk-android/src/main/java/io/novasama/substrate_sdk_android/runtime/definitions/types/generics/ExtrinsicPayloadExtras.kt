package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.Compact
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u32

object SignedExtras {

    const val MORTALITY = "CheckMortality"
    const val NONCE = "CheckNonce"
    const val TIP = "ChargeTransactionPayment"

    val default = ExtrinsicPayloadExtras(
        mapOf(
            MORTALITY to EraType,
            NONCE to Compact("Compact<Index>"),
            TIP to Compact("Compact<u32>")
        )
    )
}

object AdditionalExtras {

    const val GENESIS = "CheckGenesis"
    const val SPEC_VERSION = "CheckSpecVersion"
    const val TX_VERSION = "CheckTxVersion"
    const val BLOCK_HASH = SignedExtras.MORTALITY

    val default = ExtrinsicPayloadExtras(
        mapOf(
            BLOCK_HASH to H256,
            GENESIS to H256,
            SPEC_VERSION to u32,
            TX_VERSION to u32
        )
    )
}

typealias ExtrinsicPayloadExtrasInstance = Map<String, Any?>

class ExtrinsicPayloadExtras(
    val extras: Map<String, Type<*>>
) : Type<ExtrinsicPayloadExtrasInstance>("ExtrinsicPayloadExtras") {

    override fun decode(
        scaleCodecReader: ScaleCodecReader,
        runtime: RuntimeSnapshot
    ): ExtrinsicPayloadExtrasInstance {
        val enabledSignedExtras = runtime.metadata.extrinsic.signedExtensions

        return enabledSignedExtras.associateWith { name ->
            extras[name]?.decode(scaleCodecReader, runtime)
        }
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: ExtrinsicPayloadExtrasInstance
    ) {
        val enabledSignedExtras = runtime.metadata.extrinsic.signedExtensions

        return enabledSignedExtras.forEach { name ->
            extras[name]?.encodeUnsafe(scaleCodecWriter, runtime, value[name])
        }
    }

    override val isFullyResolved: Boolean = true

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is Map<*, *> && instance.keys.all { it is String }
    }
}
