package jp.co.soramitsu.fearless_utils.keyring.keypair

import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import jp.co.soramitsu.fearless_utils.extensions.copyLast
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import jp.co.soramitsu.fearless_utils.hash.Hasher.blake2b256
import jp.co.soramitsu.fearless_utils.hash.Hasher.keccak256
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.toAddress

typealias PublicKey = ByteArray

interface Keypair {
    val privateKey: ByteArray
    val publicKey: ByteArray

    val encryptionType: EncryptionType
}

class BaseKeypair(
    override val privateKey: ByteArray,
    override val publicKey: ByteArray,
    override val encryptionType: EncryptionType
) : Keypair

fun PublicKey.substrateAccountId(): ByteArray {
    return if (size > 32) {
        blake2b256()
    } else {
        this
    }
}

fun PublicKey.ethereumAccountId(): ByteArray {
    val decompressed = if (size == 64) {
        this
    } else {
        ECDSAUtils.decompressed(this)
    }

    return decompressed.keccak256().copyLast(20)
}

fun PublicKey.ethereumAddress(): String = ethereumAccountId().toHexString(withPrefix = true)

fun Keypair.substrateAccountId() = publicKey.substrateAccountId()
fun Keypair.ethereumAccountId() = publicKey.ethereumAccountId()
fun Keypair.ethereumAddress(): String = publicKey.ethereumAddress()
fun Keypair.ss58Address(format: Short) = publicKey.toAddress(format)
