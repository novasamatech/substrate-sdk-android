package jp.co.soramitsu.fearless_utils.signing

import jp.co.soramitsu.fearless_utils.encrypt.SignatureWrapper
import jp.co.soramitsu.fearless_utils.runtime.AccountId

class SignerPayloadRaw(
    val data: ByteArray,
    val accountId: AccountId
)

interface Signer {

    fun signRaw(payload: SignerPayloadRaw): SignatureWrapper
}

