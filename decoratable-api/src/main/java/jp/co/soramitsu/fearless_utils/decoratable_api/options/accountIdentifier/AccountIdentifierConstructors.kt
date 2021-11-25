package jp.co.soramitsu.fearless_utils.decoratable_api.options.accountIdentifier

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.config.ss58AddressOf
import jp.co.soramitsu.fearless_utils.encrypt.keypair.*
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

    suspend fun id(publicKey: PublicKey): Any

    suspend fun address(publicKey: PublicKey): String
}

suspend fun AccountIdentifierConstructor.id(account: Keypair) = id(account.publicKey)
suspend fun AccountIdentifierConstructor.address(account: Keypair) = address(account.publicKey)

class SubstrateAccountIdentifierConstructor(
    private val lookupType: String,
    private val api: SubstrateApi
) : AccountIdentifierConstructor {

    override suspend fun id(publicKey: PublicKey): Any {
        val typeRegistry = api.chainState.runtime.typeRegistry
        val addressType = typeRegistry.getOrThrow(lookupType)
        val accountId = publicKey.substrateAccountId()

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

    override suspend fun address(publicKey: PublicKey): String {
        return api.chainState.properties().ss58AddressOf(publicKey)
    }
}

class EthereumAccountIdentifierConstructor : AccountIdentifierConstructor {

    override suspend fun id(publicKey: PublicKey): Any {
        return publicKey.ethereumAccountId()
    }

    override suspend fun address(publicKey: PublicKey): String {
        return publicKey.ethereumAddress()
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


