package jp.co.soramitsu.fearless_utils.keyring.model

import jp.co.soramitsu.fearless_utils.keyring.MultiChainEncryption
import jp.co.soramitsu.fearless_utils.keyring.keypair.Keypair

class ImportAccountData(
    val keypair: Keypair,
    val multiChainEncryption: MultiChainEncryption,
    val username: String?,
    val networkTypeIdentifier: NetworkTypeIdentifier,
    val seed: ByteArray? = null
)

class ImportAccountMeta(
    val name: String?,
    val networkTypeIdentifier: NetworkTypeIdentifier,
    val encryption: MultiChainEncryption
)

sealed class NetworkTypeIdentifier {
    class Genesis(val genesis: String) : NetworkTypeIdentifier()

    class AddressByte(val addressPrefix: Short) : NetworkTypeIdentifier()

    object Undefined : NetworkTypeIdentifier()
}
