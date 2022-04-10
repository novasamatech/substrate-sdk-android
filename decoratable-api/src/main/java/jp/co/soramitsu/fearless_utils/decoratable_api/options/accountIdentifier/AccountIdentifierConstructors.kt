package jp.co.soramitsu.fearless_utils.decoratable_api.options.accountIdentifier

import jp.co.soramitsu.fearless_utils.address.AccountId
import jp.co.soramitsu.fearless_utils.address.Address
import jp.co.soramitsu.fearless_utils.address.PublicKey
import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.config.ss58AddressOf
import jp.co.soramitsu.fearless_utils.keyring.adress.EthereumAccountId
import jp.co.soramitsu.fearless_utils.keyring.adress.SubstrateAccountId
import jp.co.soramitsu.fearless_utils.keyring.adress.asEthereumAddress
import jp.co.soramitsu.fearless_utils.keyring.adress.asSubstrateAddress
import jp.co.soramitsu.fearless_utils.keyring.adress.toAccountId
import jp.co.soramitsu.fearless_utils.keyring.adress.toAddress
import jp.co.soramitsu.fearless_utils.keyring.adress.toEthereumAccountId
import jp.co.soramitsu.fearless_utils.keyring.adress.toSubstrateAccountId

interface AddressConstructor {

    companion object;

    fun interface Factory {

        fun create(api: SubstrateApi): AddressConstructor
    }

    suspend fun accountId(address: String): AccountId

    suspend fun address(accountId: AccountId): Address

    suspend fun accountId(publicKey: PublicKey): AccountId
}

suspend fun AddressConstructor.address(publicKey: PublicKey) = address(accountId(publicKey))

private class SubstrateAddressConstructor(
    private val api: SubstrateApi
) : AddressConstructor {


    override suspend fun address(accountId: AccountId): Address {
        require(accountId is SubstrateAccountId) {
            "Wrong AccountId kind was used, expected Substrate but got ${accountId::class.simpleName}"
        }

        return api.chainState.properties().ss58AddressOf(accountId)
    }

    override suspend fun accountId(publicKey: PublicKey): AccountId {
        return publicKey.toSubstrateAccountId()
    }

    override suspend fun accountId(address: String): AccountId {
        return address.asSubstrateAddress().toAccountId()
    }
}

private class EthereumAddressConstructor : AddressConstructor {

    companion object;

    override suspend fun address(accountId: AccountId): Address {
        require(accountId is EthereumAccountId) {
            "Wrong AccountId kind was used, expected Ethereum but got ${accountId::class.simpleName}"
        }

        return accountId.toAddress(withChecksum = true)
    }

    override suspend fun accountId(address: String): AccountId {
        return address.asEthereumAddress().toAccountId()
    }

    override suspend fun accountId(publicKey: PublicKey): AccountId {
        return publicKey.toEthereumAccountId()
    }
}

fun AddressConstructor.Companion.Substrate() = AddressConstructor.Factory { api ->
    SubstrateAddressConstructor(api)
}

fun AddressConstructor.Companion.Ethereum() = AddressConstructor.Factory {
    EthereumAddressConstructor()
}
