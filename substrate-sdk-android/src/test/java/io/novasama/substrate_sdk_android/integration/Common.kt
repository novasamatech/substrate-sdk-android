package io.novasama.substrate_sdk_android.integration

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.extrinsic.ExtrinsicBuilder
import io.novasama.substrate_sdk_android.runtime.metadata.callOrNull
import io.novasama.substrate_sdk_android.runtime.metadata.module
import java.math.BigInteger

const val KUSAMA_URL = "wss://kusama-rpc.polkadot.io"
const val WESTEND_URL = "wss://westend-rpc.polkadot.io"

fun ExtrinsicBuilder.transfer(
    recipientAccountId: ByteArray,
    amount: BigInteger
): ExtrinsicBuilder {
    val hasTransfer = runtime.metadata.module("Balances").callOrNull("transfer") != null
    val callName = if (hasTransfer) "transfer" else "transfer_allow_death"

    return call(
        moduleName = "Balances",
        callName = callName,
        arguments = mapOf(
            "dest" to DictEnum.Entry(
                name = "Id",
                value = recipientAccountId
            ),
            "value" to amount
        )
    )
}