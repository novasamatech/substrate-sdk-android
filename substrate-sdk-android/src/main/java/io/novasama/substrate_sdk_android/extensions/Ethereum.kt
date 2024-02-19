package io.novasama.substrate_sdk_android.extensions

import io.novasama.substrate_sdk_android.encrypt.keypair.ECDSAUtils
import io.novasama.substrate_sdk_android.hash.Hasher.keccak256
import org.web3j.crypto.Keys
import java.util.Locale

object Ethereum {

    class InvalidChecksumException : Throwable()

    inline class PublicKey(val value: ByteArray)

    inline class AccountId(val value: ByteArray)

    inline class Address(val value: String)
}

fun ByteArray.asEthereumPublicKey() = Ethereum.PublicKey(this)
fun ByteArray.asEthereumAccountId() = Ethereum.AccountId(this)
fun String.asEthereumAddress() = Ethereum.Address(this)

private val ETHEREUM_ADDRESS_REGEX = "^0x[0-9a-f]{40}$".toRegex()

fun Ethereum.Address.isValid(): Boolean {
    val withoutChecksum = value.toLowerCase(Locale.ROOT)

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

fun Ethereum.PublicKey.toAccountId(): Ethereum.AccountId {
    val decompressed = if (value.size == 64) {
        this.value
    } else {
        ECDSAUtils.decompressed(this.value)
    }

    val accountIdBytes = decompressed.keccak256().copyLast(20)

    return Ethereum.AccountId(accountIdBytes)
}

fun Ethereum.AccountId.toAddress(withChecksum: Boolean = true): Ethereum.Address {
    val inHex = value.toHexString(withPrefix = true)

    val finalAddress = if (withChecksum) {
        Keys.toChecksumAddress(inHex)
    } else {
        inHex
    }

    return Ethereum.Address(finalAddress)
}

fun Ethereum.PublicKey.toAddress(withChecksum: Boolean = true): Ethereum.Address {
    return toAccountId().toAddress(withChecksum)
}

fun Ethereum.Address.toAccountId(): Ethereum.AccountId {
    if (!isValid()) {
        throw Ethereum.InvalidChecksumException()
    }

    val accountId = value.toLowerCase(Locale.ROOT).fromHex()

    return Ethereum.AccountId(accountId)
}
