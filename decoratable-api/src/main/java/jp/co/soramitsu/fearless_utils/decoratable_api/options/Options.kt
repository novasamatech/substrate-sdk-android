package jp.co.soramitsu.fearless_utils.decoratable_api.options

import jp.co.soramitsu.fearless_utils.decoratable_api.ApiDependentFactory
import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.options.accountIdentifier.AddressConstructor
import jp.co.soramitsu.fearless_utils.decoratable_api.options.accountIdentifier.Ethereum
import jp.co.soramitsu.fearless_utils.decoratable_api.options.accountIdentifier.Substrate
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.Scale
import jp.co.soramitsu.fearless_utils.signing.Signer

class Options(
    val addressing: AddressConstructor,
    val scale: Scale = ApiScale(),
    val signer: Signer,
) {

    class Factory(
        private val accountIdConstructorFactory: AddressConstructor.Factory,
        private val signerFactory: ApiDependentFactory<Signer>,
    ) : ApiDependentFactory<Options> {

        override fun create(api: SubstrateApi) = Options(
            addressing = accountIdConstructorFactory.create(api),
            signer = signerFactory.create(api)
        )
    }

    companion object // extensions
}

fun Options.Companion.Substrate(signerFactory: ApiDependentFactory<Signer>) = Options.Factory(
    accountIdConstructorFactory = AddressConstructor.Substrate(),
    signerFactory = signerFactory
)

fun Options.Companion.Ethereum(signerFactory: ApiDependentFactory<Signer>) = Options.Factory(
    accountIdConstructorFactory = AddressConstructor.Ethereum(),
    signerFactory = signerFactory
)
