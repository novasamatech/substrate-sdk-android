package io.novasama.substrate_sdk_android.integration

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.extrinsic.ExtrinsicBuilder
import java.math.BigInteger

const val KUSAMA_URL = "wss://kusama-rpc.polkadot.io"
const val WESTEND_URL = "wss://westend-rpc.polkadot.io"

fun ExtrinsicBuilder.transfer(
    recipientAccountId: ByteArray,
    amount: BigInteger
): ExtrinsicBuilder {
    return call(
        moduleName = "Balances",
        callName = "transfer",
        arguments = mapOf(
            "dest" to DictEnum.Entry(
                name = "Id",
                value = recipientAccountId
            ),
            "value" to amount
        )
    )
}