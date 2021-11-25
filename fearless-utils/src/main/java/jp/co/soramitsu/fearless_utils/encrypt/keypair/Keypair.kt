package jp.co.soramitsu.fearless_utils.encrypt.keypair

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.extensions.copyLast
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import jp.co.soramitsu.fearless_utils.hash.Hasher.blake2b256
import jp.co.soramitsu.fearless_utils.hash.Hasher.keccak256
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.toAddress
import java.security.Key
import java.security.KeyPair

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

fun Keypair.substrateAccountId(): ByteArray {
   return if (publicKey.size > 32) {
        publicKey.blake2b256()
    } else {
        publicKey
    }
}

fun Keypair.ethereumAccountId(): ByteArray {
    val decompressed = if (publicKey.size == 64) {
        publicKey
    } else {
        ECDSAUtils.decompressed(publicKey)
    }

    return decompressed.keccak256().copyLast(20)
}

fun Keypair.ethereumAddress(): String {
    return ethereumAccountId().toHexString(withPrefix = true)
}

fun Keypair.ss58Address(format: Short) = publicKey.toAddress(format)
