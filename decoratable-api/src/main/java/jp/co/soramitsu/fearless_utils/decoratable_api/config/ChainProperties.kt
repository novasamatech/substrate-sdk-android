package jp.co.soramitsu.fearless_utils.decoratable_api.config

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.chain.chain
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.chain.getBlockHash
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.system.properties
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.system.system
import jp.co.soramitsu.fearless_utils.decoratable_api.util.ext.lazyAsync
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.ss58.DEFAULT_PREFIX
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder
import kotlinx.coroutines.GlobalScope
import java.math.BigInteger
import java.security.KeyPair
import jp.co.soramitsu.fearless_utils.decoratable_api.config.ChainProperties as IChainProperties

private const val DEFAULT_SYMBOL = "DEV"

class ChainStateImpl(
    private val api: SubstrateApi,
    override val runtime: RuntimeSnapshot
) : ChainState {

    private val chainProperties by GlobalScope.lazyAsync {
        val propertiesFromRpc = api.rpc.system.properties()

        ChainProperties(
            ss58Format = propertiesFromRpc.ss58Format ?: SS58Encoder.DEFAULT_PREFIX,
            tokenDecimals = propertiesFromRpc.tokenDecimals,
            tokenSymbol = propertiesFromRpc.tokenSymbol ?: DEFAULT_SYMBOL
        )
    }

    private val genesisHash by GlobalScope.lazyAsync {
        api.rpc.chain.getBlockHash(BigInteger.ZERO)
    }

    override suspend fun properties(): IChainProperties = chainProperties.await()

    override suspend fun genesisHash(): String = genesisHash.await()

    private data class ChainProperties(
        override val ss58Format: Short,
        override val tokenDecimals: Int,
        override val tokenSymbol: String
    ) : IChainProperties
}


