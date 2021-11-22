package jp.co.soramitsu.fearless_utils.decoratable_api.tx.mortality

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.const.babe.babeOrNull
import jp.co.soramitsu.fearless_utils.decoratable_api.const.babe.expectedBlockTime
import jp.co.soramitsu.fearless_utils.decoratable_api.const.system.blockHashCountOrNull
import jp.co.soramitsu.fearless_utils.decoratable_api.const.system.systemOrNull
import jp.co.soramitsu.fearless_utils.decoratable_api.const.timestamp.minimumPeriod
import jp.co.soramitsu.fearless_utils.decoratable_api.const.timestamp.timestampOrNull
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.chain.chain
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.chain.getBlockHash
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.chain.getFinalizedHead
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.chain.getHeader
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Era

private const val FALLBACK_MAX_HASH_COUNT = 250
private const val MAX_FINALITY_LAG = 5
private const val FALLBACK_PERIOD = 6 * 1000
private const val MORTAL_PERIOD = 5 * 60 * 1000

object MortalityConstructor {

    data class Mortality(val era: Era.Mortal, val blockHash: String)

    suspend fun constructMortality(
        api: SubstrateApi
    ): Mortality {
        val finalizedHash = api.rpc.chain.getFinalizedHead(null)

        val bestHeader = api.rpc.chain.getHeader(null)
        val finalizedHeader = api.rpc.chain.getHeader(finalizedHash)

        val currentHeader = bestHeader.parentHash?.let {
            api.rpc.chain.getHeader(it)
        } ?: bestHeader

        val currentNumber = currentHeader.parsedNumber()
        val finalizedNumber = finalizedHeader.parsedNumber()

        val startBlockNumber = if (currentNumber - finalizedNumber > MAX_FINALITY_LAG) currentNumber else finalizedNumber

        val blockHashCount = api.const.systemOrNull?.blockHashCountOrNull?.value?.toInt() ?: FALLBACK_MAX_HASH_COUNT

        val blockTime = api.const.babeOrNull?.expectedBlockTime?.value?.toInt()
            ?: api.const.timestampOrNull?.minimumPeriod?.value?.toInt()
            ?: FALLBACK_PERIOD

        val mortalPeriod = MORTAL_PERIOD / blockTime + MAX_FINALITY_LAG

        val unmappedPeriod = minOf(blockHashCount, mortalPeriod)

        val era = Era.getEraFromBlockPeriod(startBlockNumber, unmappedPeriod)
        val eraBlockNumber = ((startBlockNumber - era.phase) / era.period) * era.period + era.phase

        val eraBlockHash = api.rpc.chain.getBlockHash(eraBlockNumber.toBigInteger())

        return Mortality(era, eraBlockHash)
    }
}
