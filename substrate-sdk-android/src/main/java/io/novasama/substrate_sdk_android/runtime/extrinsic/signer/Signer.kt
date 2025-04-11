package io.novasama.substrate_sdk_android.runtime.extrinsic.signer

import io.novasama.substrate_sdk_android.encrypt.SignatureWrapper
import io.novasama.substrate_sdk_android.extensions.fromHex
import io.novasama.substrate_sdk_android.runtime.AccountId

class SignedRaw(
    val payload: SignerPayloadRaw,
    val signatureWrapper: SignatureWrapper
)

class SignerPayloadRaw(
    val message: ByteArray,
    val accountId: AccountId,
    val skipMessageHashing: Boolean = false,
) {
    companion object;
}

fun SignerPayloadRaw.Companion.fromUtf8(
    utf8Message: String,
    accountId: AccountId,
    skipMessageHashing: Boolean = false
) = SignerPayloadRaw(utf8Message.encodeToByteArray(), accountId, skipMessageHashing)

fun SignerPayloadRaw.Companion.fromHex(
    hexMessage: String,
    accountId: AccountId,
    skipMessageHashing: Boolean = false
) = SignerPayloadRaw(hexMessage.fromHex(), accountId, skipMessageHashing)

interface Signer {

    suspend fun signRaw(payload: SignerPayloadRaw): SignedRaw
}
