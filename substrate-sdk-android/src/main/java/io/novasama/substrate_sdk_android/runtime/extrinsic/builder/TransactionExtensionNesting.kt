package io.novasama.substrate_sdk_android.runtime.extrinsic.builder

import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionId

interface TransactionExtensionNesting {

    fun nestedLevelOf(transactionExtensionId: TransactionExtensionId): Int
}

class FlatTransactionExtensionNesting : TransactionExtensionNesting {
    override fun nestedLevelOf(transactionExtensionId: TransactionExtensionId): Int {
        return 0
    }
}
