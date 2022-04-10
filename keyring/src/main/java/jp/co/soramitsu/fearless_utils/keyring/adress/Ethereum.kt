package jp.co.soramitsu.fearless_utils.keyring.adress

import jp.co.soramitsu.fearless_utils.address.AccountId
import jp.co.soramitsu.fearless_utils.address.Address
import jp.co.soramitsu.fearless_utils.address.PublicKey
import jp.co.soramitsu.fearless_utils.extensions.copyLast
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import jp.co.soramitsu.fearless_utils.hash.Hasher.keccak256
import jp.co.soramitsu.fearless_utils.keyring.keypair.ECDSAUtils
import org.web3j.crypto.Keys
import java.util.Locale

@JvmInline
value class EthereumAccountId(override val value: ByteArray) : AccountId

fun PublicKey.toEthereumAccountId(): EthereumAccountId {
    val decompressed = if (value.size == 64) {
        this.value
    } else {
        ECDSAUtils.decompressed(this.value)
    }

    val accountIdBytes = decompressed.keccak256().copyLast(20)

    return EthereumAccountId(accountIdBytes)
}

@JvmInline
value class EthereumAddress(override val value: String) : Address {

    override fun isValid(): Boolean {
        val withoutChecksum = value.lowercase(Locale.ROOT)

        // value is hex string of 40 symbols
        if (!ETHEREUM_ADDRESS_REGEX.matches(withoutChecksum)) {
            return false
        }

        return when {
            withoutChecksum == value -> true
            Keys.toChecksumAddress(withoutChecksum) == value -> true
            else -> false
        }
    }
}

fun EthereumAddress.toAccountId(): AccountId {
    if (!isValid()) {
        throw InvalidEthereumChecksumException()
    }

    val accountId = value.lowercase().fromHex()

    return EthereumAccountId(accountId)
}

class InvalidEthereumChecksumException : Throwable()

fun ByteArray.asEthereumAccountId() = EthereumAccountId(this)
fun String.asEthereumAddress() = EthereumAddress(this)

private val ETHEREUM_ADDRESS_REGEX = "^0x[0-9a-f]{40}$".toRegex()

fun EthereumAccountId.toAddress(withChecksum: Boolean = true): EthereumAddress {
    val inHex = value.toHexString(withPrefix = true)

    val finalAddress = if (withChecksum) {
        Keys.toChecksumAddress(inHex)
    } else {
        inHex
    }

    return EthereumAddress(finalAddress)
}
