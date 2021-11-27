package jp.co.soramitsu.fearless_utils.decoratable_api.options

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.options.accountIdentifier.AccountIdentifierConstructor
import jp.co.soramitsu.fearless_utils.decoratable_api.options.accountIdentifier.defaultSubstrate
import jp.co.soramitsu.fearless_utils.decoratable_api.options.accountIdentifier.ethereum
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.Scale

class Options(
    val accountIdentifierConstructor: AccountIdentifierConstructor,
    val scale: Scale = ApiScale()
) {

    class Factory(
        private val accountIdConstructorFactory: AccountIdentifierConstructor.Factory,
    ) {

        fun build(api: SubstrateApi) = Options(
            accountIdentifierConstructor = accountIdConstructorFactory.create(api)
        )
    }

    companion object // extensions
}

fun Options.Companion.substrate() = Options.Factory(
    accountIdConstructorFactory = AccountIdentifierConstructor.defaultSubstrate(),
)

fun Options.Companion.ethereum() = Options.Factory(
    accountIdConstructorFactory = AccountIdentifierConstructor.ethereum(),
)
