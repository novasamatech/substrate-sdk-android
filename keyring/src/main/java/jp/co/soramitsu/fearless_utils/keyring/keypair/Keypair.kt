package jp.co.soramitsu.fearless_utils.keyring.keypair

import jp.co.soramitsu.fearless_utils.address.PublicKey
import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import jp.co.soramitsu.fearless_utils.extensions.copyLast
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import jp.co.soramitsu.fearless_utils.hash.Hasher.blake2b256
import jp.co.soramitsu.fearless_utils.hash.Hasher.keccak256
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder.toAddress

interface Keypair {
    val privateKey: ByteArray
    val publicKey: PublicKey

    val encryptionType: EncryptionType
}

class BaseKeypair(
    override val privateKey: ByteArray,
    override val publicKey: PublicKey,
    override val encryptionType: EncryptionType
) : Keypair
