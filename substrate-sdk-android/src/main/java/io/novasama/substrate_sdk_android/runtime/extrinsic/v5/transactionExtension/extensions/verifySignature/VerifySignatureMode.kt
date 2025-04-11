package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature

import io.novasama.substrate_sdk_android.runtime.AccountId

sealed class VerifySignatureMode {

    companion object {
        fun from(signer: GeneralTransactionSigner?, accountId: AccountId?): VerifySignatureMode {
            return if (accountId != null && signer != null) {
                Enabled(signer, accountId)
            } else {
                Disabled
            }
        }
    }

    class Enabled(val signer: GeneralTransactionSigner, val accountId: AccountId) : VerifySignatureMode()

    object Disabled : VerifySignatureMode()
}

fun VerifySignatureMode.enabledOrThrow(): VerifySignatureMode.Enabled {
    require(this is VerifySignatureMode.Enabled) {
        "VerifySignature is Disabled"
    }

    return this
}
