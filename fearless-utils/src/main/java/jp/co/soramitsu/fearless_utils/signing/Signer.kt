package jp.co.soramitsu.fearless_utils.signing

import jp.co.soramitsu.fearless_utils.address.AccountId

class SignerPayloadRaw(
    val data: ByteArray,
    val origin: AccountId
)

class MultiSignature(val encryptionType: String, val signature: ByteArray)

interface Signer {

    suspend fun signRaw(payload: SignerPayloadRaw): MultiSignature
}
