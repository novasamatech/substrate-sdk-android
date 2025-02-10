package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions

import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.DefaultSignedExtensions

class CheckGenesis(val genesisHash: ByteArray) : FixedValueTransactionExtension(
    name = DefaultSignedExtensions.CHECK_GENESIS,
    implicit = genesisHash,
    explicit = null
)
