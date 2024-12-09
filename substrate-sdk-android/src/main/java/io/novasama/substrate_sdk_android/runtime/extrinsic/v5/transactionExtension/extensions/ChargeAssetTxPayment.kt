package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions

import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.Struct
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension
import java.math.BigInteger

fun ChargeAssetTxPayment(
    tip: BigInteger,
    assetId: Any?
) = TransactionExtension(
    name = "ChargeAssetTxPayment",
    explicit = Struct.Instance(
        mapOf(
            "tip" to tip,
            "assetId" to assetId
        )
    ),
    implicit = null
)