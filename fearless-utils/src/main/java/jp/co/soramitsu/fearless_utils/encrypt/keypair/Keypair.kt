package jp.co.soramitsu.fearless_utils.encrypt.keypair

import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType

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
