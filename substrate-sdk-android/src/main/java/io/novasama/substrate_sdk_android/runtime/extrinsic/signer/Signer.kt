package io.novasama.substrate_sdk_android.runtime.extrinsic.signer

import io.novasama.substrate_sdk_android.encrypt.SignatureWrapper
import io.novasama.substrate_sdk_android.extensions.fromHex
import io.novasama.substrate_sdk_android.runtime.AccountId
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Extrinsic.EncodingInstance.CallRepresentation
import io.novasama.substrate_sdk_android.runtime.extrinsic.Nonce

class SignedExtrinsic(
    val payload: SignerPayloadExtrinsic,
    val signatureWrapper: SignatureWrapper
)

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

data class SignerPayloadExtrinsic(
    val runtime: RuntimeSnapshot,
    val accountId: AccountId,
    val call: CallRepresentation,
    val signedExtras: SignedExtras,
    val nonce: Nonce,
) {

    data class SignedExtras(
        val includedInExtrinsic: Map<String, Any?>,
        val includedInSignature: Map<String, Any?>,
    )
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

    suspend fun signExtrinsic(payloadExtrinsic: SignerPayloadExtrinsic): SignedExtrinsic

    suspend fun signRaw(payload: SignerPayloadRaw): SignedRaw
}
