package jp.co.soramitsu.fearless_utils.decoratable_api.config

import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot

interface ChainConfiguration {

    val runtime: RuntimeSnapshot

    suspend fun chainProperties(): ChainProperties
}

interface ChainProperties {

    val ss58Format: Short

    val tokenDecimals: Int

    val tokenSymbol: String
}
