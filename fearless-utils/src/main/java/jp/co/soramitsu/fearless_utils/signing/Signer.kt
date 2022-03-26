package jp.co.soramitsu.fearless_utils.signing

import jp.co.soramitsu.fearless_utils.runtime.AccountId

class SignerPayloadRaw(
    val data: ByteArray,
    val origin: AccountId
)

class MultiSignature(val encryptionType: String, val signature: ByteArray)

interface Signer {

    fun signRaw(payload: SignerPayloadRaw): MultiSignature
}
