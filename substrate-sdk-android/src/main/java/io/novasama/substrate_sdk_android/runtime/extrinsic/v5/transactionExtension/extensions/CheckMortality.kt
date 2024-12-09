package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions

import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.DefaultSignedExtensions
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Era
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension

fun CheckMortality(
    era: Era,
    blockHash: ByteArray
) = TransactionExtension(
    name = DefaultSignedExtensions.CHECK_MORTALITY,
    implicit = blockHash,
    explicit = era
)