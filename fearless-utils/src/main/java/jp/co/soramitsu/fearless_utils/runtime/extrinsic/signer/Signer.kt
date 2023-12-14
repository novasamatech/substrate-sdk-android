package jp.co.soramitsu.fearless_utils.runtime.extrinsic.signer

import jp.co.soramitsu.fearless_utils.encrypt.SignatureWrapper
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.runtime.AccountId
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Extrinsic.EncodingInstance.CallRepresentation

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
    val signedExtras: Map<String, Any?>,
    val additionalSignedExtras: Map<String, Any?>
)

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
