package jp.co.soramitsu.fearless_utils.address

@JvmInline
value class PublicKey(val value: ByteArray)

fun ByteArray.asPublicKey() = PublicKey(this)

interface AccountId {

    val value: ByteArray
}

interface Address {

    fun isValid(): Boolean

    val value: String
}
