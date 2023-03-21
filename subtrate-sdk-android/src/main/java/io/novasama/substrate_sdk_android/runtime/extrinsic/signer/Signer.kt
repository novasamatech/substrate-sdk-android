package io.novasama.substrate_sdk_android.runtime.extrinsic.signer

import io.novasama.substrate_sdk_android.encrypt.SignatureWrapper
import io.novasama.substrate_sdk_android.extensions.fromHex
import io.novasama.substrate_sdk_android.runtime.AccountId
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Extrinsic
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Extrinsic.EncodingInstance.CallRepresentation

class SignerPayloadRaw(
    val message: ByteArray,
    val accountId: AccountId,
    val skipMessageHashing: Boolean = false,
) {
    companion object;
}

class SignerPayloadExtrinsic(
    val runtime: RuntimeSnapshot,
    val extrinsicType: Extrinsic,

    val accountId: AccountId,

    val call: CallRepresentation,
    val signedExtras: Map<String, Any?>,
    val additionalExtras: Map<String, Any?>
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

    suspend fun signExtrinsic(payloadExtrinsic: SignerPayloadExtrinsic): SignatureWrapper

    suspend fun signRaw(payload: SignerPayloadRaw): SignatureWrapper
}
