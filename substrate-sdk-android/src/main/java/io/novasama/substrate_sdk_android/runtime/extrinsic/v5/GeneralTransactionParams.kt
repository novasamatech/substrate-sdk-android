package io.novasama.substrate_sdk_android.runtime.extrinsic.v5

import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericCall
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.AbsentExtension
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionId

class GeneralTransactionParams(
    val extensions: Map<TransactionExtensionId, TransactionExtension>,
    val call: GenericCall.Instance
)

fun Map<TransactionExtensionId, TransactionExtension>.getOrAbsent(id: TransactionExtensionId): TransactionExtension {
    return getOrElse(id) { AbsentExtension(id) }
}