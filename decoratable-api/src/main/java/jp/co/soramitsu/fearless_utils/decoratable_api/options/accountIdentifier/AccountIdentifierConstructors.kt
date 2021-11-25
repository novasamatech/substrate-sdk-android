package jp.co.soramitsu.fearless_utils.decoratable_api.options.accountIdentifier

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.config.ss58AddressOf
import jp.co.soramitsu.fearless_utils.encrypt.keypair.Keypair
import jp.co.soramitsu.fearless_utils.encrypt.keypair.ethereumAccountId
import jp.co.soramitsu.fearless_utils.encrypt.keypair.ethereumAddress
import jp.co.soramitsu.fearless_utils.encrypt.keypair.substrateAccountId
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.getOrThrow
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.MULTI_ADDRESS_ID


interface AccountIdentifierConstructor {

    fun interface Factory {

        fun create(api: SubstrateApi): AccountIdentifierConstructor
    }

    companion object {
        const val DEFAULT_ADDRESS_TYPE = "Address"
    }

    suspend fun id(keypair: Keypair): Any

    suspend fun address(keypair: Keypair): String
}

class SubstrateAccountIdentifierConstructor(
    private val lookupType: String,
    private val api: SubstrateApi
) : AccountIdentifierConstructor {

    override suspend fun id(keypair: Keypair): Any {
        val typeRegistry = api.chainState.runtime.typeRegistry
        val addressType = typeRegistry.getOrThrow(lookupType)
        val accountId = keypair.substrateAccountId()

        return when {
            addressType is DictEnum && addressType.variantOrNull(MULTI_ADDRESS_ID) != null -> { // MultiAddress
                DictEnum.Entry(MULTI_ADDRESS_ID, accountId)
            }
            addressType.isValidInstance(accountId) -> { // GenericAccountId or similar
                accountId
            }
            else -> throw UnsupportedOperationException("Unknown address type: ${addressType.name}")
        }
    }

    override suspend fun address(keypair: Keypair): String {
        return api.chainState.properties().ss58AddressOf(keypair)
    }
}

class EthereumAccountIdentifierConstructor : AccountIdentifierConstructor {

    override suspend fun id(keypair: Keypair): Any {
        return keypair.ethereumAccountId()
    }

    override suspend fun address(keypair: Keypair): String {
        return keypair.ethereumAddress()
    }
}

fun AccountIdentifierConstructor.Companion.defaultSubstrate(
    addressTypeName: String = DEFAULT_ADDRESS_TYPE
) = AccountIdentifierConstructor.Factory { api ->
    SubstrateAccountIdentifierConstructor(addressTypeName, api)
}

fun AccountIdentifierConstructor.Companion.ethereum() = AccountIdentifierConstructor.Factory {
    EthereumAccountIdentifierConstructor()
}


