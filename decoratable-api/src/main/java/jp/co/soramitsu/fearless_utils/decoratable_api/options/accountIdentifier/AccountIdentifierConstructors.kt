package jp.co.soramitsu.fearless_utils.decoratable_api.options.accountIdentifier

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.config.ss58AddressOf
import jp.co.soramitsu.fearless_utils.keyring.keypair.PublicKey
import jp.co.soramitsu.fearless_utils.keyring.keypair.ethereumAccountId
import jp.co.soramitsu.fearless_utils.keyring.keypair.ethereumAddress
import jp.co.soramitsu.fearless_utils.keyring.keypair.substrateAccountId
import jp.co.soramitsu.fearless_utils.runtime.AccountId
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.getOrThrow
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.MULTI_ADDRESS_ID

interface AccountIdentifierConstructor {

    class Identifier(
        val accountId: AccountId,
        val forEncoding: Any
    )

    fun interface Factory {

        fun create(api: SubstrateApi): AccountIdentifierConstructor
    }

    companion object {
        const val DEFAULT_ADDRESS_TYPE = "Address"
    }

    suspend fun identifier(publicKey: PublicKey): Identifier

    suspend fun address(publicKey: PublicKey): String
}

class SubstrateAccountIdentifierConstructor(
    private val lookupType: String,
    private val api: SubstrateApi
) : AccountIdentifierConstructor {

    override suspend fun identifier(publicKey: PublicKey): AccountIdentifierConstructor.Identifier {
        val typeRegistry = api.chainState.runtime.typeRegistry
        val addressType = typeRegistry.getOrThrow(lookupType)
        val accountId = publicKey.substrateAccountId()

        val identifierForEncoding: Any = when {
            addressType is DictEnum && addressType.variantOrNull(MULTI_ADDRESS_ID) != null -> { // MultiAddress
                DictEnum.Entry(MULTI_ADDRESS_ID, accountId)
            }
            addressType.isValidInstance(accountId) -> { // GenericAccountId or similar
                accountId
            }
            else -> throw UnsupportedOperationException("Unknown address type: ${addressType.name}")
        }

        return AccountIdentifierConstructor.Identifier(accountId, identifierForEncoding)
    }

    override suspend fun address(publicKey: PublicKey): String {
        return api.chainState.properties().ss58AddressOf(publicKey)
    }
}

class EthereumAccountIdentifierConstructor : AccountIdentifierConstructor {

    override suspend fun identifier(publicKey: PublicKey): AccountIdentifierConstructor.Identifier {
        val accountId = publicKey.ethereumAccountId()

        return AccountIdentifierConstructor.Identifier(accountId = accountId, forEncoding = accountId)
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

fun AccountIdentifierConstructor.Companion.Ethereum() = AccountIdentifierConstructor.Factory {
    EthereumAccountIdentifierConstructor()
}
