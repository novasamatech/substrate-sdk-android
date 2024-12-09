package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature

import io.novasama.substrate_sdk_android.runtime.AccountId

sealed class VerifySignatureMode {

    class Enabled(val signer: GeneralTransactionSigner, val accountId: AccountId) : VerifySignatureMode()

    object Disabled : VerifySignatureMode()
}