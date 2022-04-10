package jp.co.soramitsu.fearless_utils.keyring.keypair.ethereum

import jp.co.soramitsu.fearless_utils.address.PublicKey
import jp.co.soramitsu.fearless_utils.keyring.EncryptionType
import jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair

class Bip32ExtendedKeyPair(
    override val privateKey: ByteArray,
    override val publicKey: PublicKey,
    val chaincode: ByteArray
) : Keypair {

    override val encryptionType = EncryptionType.ECDSA
}
