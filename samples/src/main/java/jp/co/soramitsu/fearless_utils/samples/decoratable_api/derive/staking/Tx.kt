package jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.staking

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApiException
import jp.co.soramitsu.fearless_utils.decoratable_api.moduleNotFound
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.*
import java.math.BigInteger

interface StakingFunctions : DecoratableFunctions

typealias BalanceOf = BigInteger

val DecoratableTx.stakingOrNull: StakingFunctions?
    get() = decorate("Staking") {
        object : StakingFunctions, DecoratableFunctions by this {}
    }

val DecoratableTx.staking: StakingFunctions
    get() = stakingOrNull ?: SubstrateApiException.moduleNotFound("Staking")

val StakingFunctions.chill: Function0
    get() = decorator.function0("chill")

val StakingFunctions.unbond: Function1<BalanceOf>
    get() = decorator.function1("unbond")
