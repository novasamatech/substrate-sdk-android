package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature

import io.novasama.substrate_sdk_android.runtime.AccountId

internal class SignatureInstance(
    val signature: Any?,
    val accountId: AccountId
)
