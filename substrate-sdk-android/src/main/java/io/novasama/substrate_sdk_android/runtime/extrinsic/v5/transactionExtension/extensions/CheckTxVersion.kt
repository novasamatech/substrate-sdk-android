package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions

import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.DefaultSignedExtensions
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension

fun CheckTxVersion(txVersion: Int) = TransactionExtension(
    name = DefaultSignedExtensions.CHECK_TX_VERSION,
    implicit = txVersion.toBigInteger(),
    explicit = null
)
