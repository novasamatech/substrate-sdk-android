package jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.Compact
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u32
import jp.co.soramitsu.fearless_utils.runtime.metadata.SignedExtensionMetadata
import jp.co.soramitsu.fearless_utils.runtime.metadata.SignedExtensionMetadata.Companion.onlyAdditional
import jp.co.soramitsu.fearless_utils.runtime.metadata.SignedExtensionMetadata.Companion.onlySigned

object DefaultSignedExtensions {

    const val CHECK_MORTALITY = "CheckMortality"
    const val CHECK_NONCE = "CheckNonce"
    const val CHECK_TX_PAYMENT = "ChargeTransactionPayment"
    const val CHECK_GENESIS = "CheckGenesis"
    const val CHECK_SPEC_VERSION = "CheckSpecVersion"
    const val CHECK_TX_VERSION = "CheckTxVersion"

    val ALL = listOf(
        SignedExtensionMetadata(CHECK_MORTALITY, type = EraType, additionalSigned = H256),
        onlySigned(CHECK_NONCE, Compact("Compact<Index>")),
        onlySigned(CHECK_TX_PAYMENT, Compact("Compact<u32>")),
        onlyAdditional(CHECK_GENESIS, H256),
        onlyAdditional(CHECK_SPEC_VERSION, u32),
        onlyAdditional(CHECK_TX_VERSION, u32),
    )
}