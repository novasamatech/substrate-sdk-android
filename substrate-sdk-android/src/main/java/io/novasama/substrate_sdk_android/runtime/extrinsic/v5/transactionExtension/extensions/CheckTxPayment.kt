package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions

import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.DefaultSignedExtensions
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension
import java.math.BigInteger

fun ChargeTransactionPayment(
   tip: BigInteger
) = TransactionExtension(
    name = DefaultSignedExtensions.CHECK_TX_PAYMENT,
    implicit = null,
    explicit = tip
)