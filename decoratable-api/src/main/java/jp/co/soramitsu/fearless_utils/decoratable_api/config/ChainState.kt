package jp.co.soramitsu.fearless_utils.decoratable_api.config

import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot

interface ChainState {

    val runtime: RuntimeSnapshot

    suspend fun properties(): ChainProperties

    suspend fun genesisHash(): String
}

interface ChainProperties {

    val ss58Format: Short

    val tokenDecimals: Int

    val tokenSymbol: String
}
