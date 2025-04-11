package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature

import io.novasama.substrate_sdk_android.encrypt.SignatureWrapper
import io.novasama.substrate_sdk_android.runtime.AccountId
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.InheritedImplication

interface GeneralTransactionSigner {

    suspend fun signInheritedImplication(
        inheritedImplication: InheritedImplication,
        accountId: AccountId,
    ): SignatureWrapper
}
