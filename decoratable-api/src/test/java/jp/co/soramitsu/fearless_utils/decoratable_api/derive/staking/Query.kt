package jp.co.soramitsu.fearless_utils.decoratable_api.derive.staking

import jp.co.soramitsu.fearless_utils.runtime.AccountId
import jp.co.soramitsu.fearless_utils.decoratable_api.query.DecoratableQuery
import jp.co.soramitsu.fearless_utils.decoratable_api.query.DecoratableStorage
import java.math.BigInteger

fun bindNumber(dynamicInstance: Any?): BigInteger = dynamicInstance as BigInteger

fun bindAccountId(dynamicInstance: Any?) = dynamicInstance as AccountId

interface StakingStorage : DecoratableStorage

val DecoratableQuery.staking: StakingStorage
    get() = decorate("Staking") {
        object : StakingStorage, DecoratableStorage by this {}
    }

val StakingStorage.historyDepth
    get() = decorator.plain("HistoryDepth", ::bindNumber)

val StakingStorage.bonded
    get() = decorator.single<AccountId, AccountId>("Bonded", ::bindAccountId)
