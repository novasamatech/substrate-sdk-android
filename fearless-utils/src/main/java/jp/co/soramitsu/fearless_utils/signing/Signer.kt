package jp.co.soramitsu.fearless_utils.signing

import jp.co.soramitsu.fearless_utils.runtime.AccountId

class SignerPayloadRaw(
    val data: ByteArray,
    val accountId: AccountId
)

class SignerResult(
    val signature: ByteArray,
    val encryptionType: String
)

interface Signer {

    fun signRaw(payload: SignerPayloadRaw): SignerResult
}

