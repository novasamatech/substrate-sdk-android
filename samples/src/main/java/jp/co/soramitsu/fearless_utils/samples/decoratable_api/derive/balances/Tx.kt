package jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.balances

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApiException
import jp.co.soramitsu.fearless_utils.decoratable_api.moduleNotFound
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.DecoratableFunctions
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.DecoratableTx
import jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.staking.BalanceOf
import jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.types.MultiAddress

interface BalancesFunctions : DecoratableFunctions

val DecoratableTx.balancesOrNull: BalancesFunctions?
    get() = decorate("Balances") {
        object : BalancesFunctions, DecoratableFunctions by this {}
    }

val DecoratableTx.balances: BalancesFunctions
    get() = balancesOrNull ?: SubstrateApiException.moduleNotFound("Balances")

val BalancesFunctions.transfer
    get() = with(decorator) {
        function2<MultiAddress, BalanceOf>("transfer")
    }
