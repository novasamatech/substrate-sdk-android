package jp.co.soramitsu.fearless_utils.runtime.extrinsic.signer

import jp.co.soramitsu.fearless_utils.encrypt.SignatureWrapper
import jp.co.soramitsu.fearless_utils.runtime.AccountId

class SignerPayloadRaw(
    val message: ByteArray,
    val accountId: AccountId,
    val skipMessageHashing: Boolean,
)

interface Signer {

    suspend fun signRaw(payload: SignerPayloadRaw): SignatureWrapper
}
