@file:UseSerializers(BigIntegerSerializer::class)

package jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.staking

import jp.co.soramitsu.fearless_utils.decoratable_api.query.DecoratableQuery
import jp.co.soramitsu.fearless_utils.decoratable_api.query.DecoratableStorage
import jp.co.soramitsu.fearless_utils.decoratable_api.query.PlainStorageEntry
import jp.co.soramitsu.fearless_utils.decoratable_api.query.SingleMapStorageEntry
import jp.co.soramitsu.fearless_utils.decoratable_api.query.map1
import jp.co.soramitsu.fearless_utils.decoratable_api.query.plain
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.serializers.BigIntegerSerializer
import jp.co.soramitsu.fearless_utils.runtime.AccountId
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigInteger

@Serializable
data class StakingLedger(
    val stash: AccountId,
    val total: BigInteger,
    val active: BigInteger,
    val unlocking: List<UnlockChunk>,
    val claimedRewards: List<BigInteger>
)

@Serializable
data class UnlockChunk(val amount: BigInteger, val era: BigInteger)

interface StakingStorage : DecoratableStorage

val DecoratableQuery.staking: StakingStorage
    get() = decorate("Staking") {
        object : StakingStorage, DecoratableStorage by this {}
    }

val StakingStorage.historyDepth: PlainStorageEntry<BigInteger>
    get() = decorator.plain("HistoryDepth")

val StakingStorage.bonded: SingleMapStorageEntry<AccountId, AccountId>
    get() = decorator.map1("Bonded")

val StakingStorage.ledger: SingleMapStorageEntry<AccountId, StakingLedger>
    get() = decorator.map1("Ledger")
