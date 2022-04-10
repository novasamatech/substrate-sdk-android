package jp.co.soramitsu.fearless_utils.keyring.adress

import jp.co.soramitsu.fearless_utils.address.AccountId
import jp.co.soramitsu.fearless_utils.address.Address
import jp.co.soramitsu.fearless_utils.address.PublicKey
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.publicKeyToAccountId
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.toAccountId
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.toAddress

private const val DEFAULT_FORMAT: Short = 42

fun PublicKey.toSubstrateAccountId(): SubstrateAccountId = value.publicKeyToAccountId().asSubstrateAccountId()

@JvmInline
value class SubstrateAccountId(override val value: ByteArray) : AccountId

@JvmInline
value class SubstrateAddress(override val value: String) : Address {

    override fun isValid(): Boolean {
        return runCatching { toAccountId() }.isSuccess
    }

    fun toAccountId(): AccountId {
        return value.toAccountId().asSubstrateAccountId()
    }
}

fun ByteArray.asSubstrateAccountId() = SubstrateAccountId(this)
fun String.asSubstrateAddress() = SubstrateAddress(this)

fun SubstrateAccountId.toAddress(format: Short = DEFAULT_FORMAT): SubstrateAddress {
    return value.toAddress(format).asSubstrateAddress()
}
