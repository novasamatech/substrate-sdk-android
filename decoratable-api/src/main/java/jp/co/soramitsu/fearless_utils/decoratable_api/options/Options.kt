package jp.co.soramitsu.fearless_utils.decoratable_api.options

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.options.accountIdentifier.AccountIdentifierConstructor
import jp.co.soramitsu.fearless_utils.decoratable_api.options.accountIdentifier.defaultSubstrate
import jp.co.soramitsu.fearless_utils.decoratable_api.options.accountIdentifier.Ethereum
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.Scale
import jp.co.soramitsu.fearless_utils.signing.Signer

class Options(
    val accountIdentifierConstructor: AccountIdentifierConstructor,
    val scale: Scale = ApiScale(),
    val signer: Signer,
) {

    class Factory(
        private val accountIdConstructorFactory: AccountIdentifierConstructor.Factory,
        private val signer: Signer,
    ) {

        fun build(api: SubstrateApi) = Options(
            accountIdentifierConstructor = accountIdConstructorFactory.create(api),
            signer = signer
        )
    }

    companion object // extensions
}

fun Options.Companion.Substrate(signer: Signer) = Options.Factory(
    accountIdConstructorFactory = AccountIdentifierConstructor.defaultSubstrate(),
    signer = signer
)

fun Options.Companion.Ethereum(signer: Signer) = Options.Factory(
    accountIdConstructorFactory = AccountIdentifierConstructor.Ethereum(),
    signer = signer
)
