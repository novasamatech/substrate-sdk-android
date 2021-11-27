package jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.utility

import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApiException
import jp.co.soramitsu.fearless_utils.decoratable_api.moduleNotFound
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericCall
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.DecoratableFunctions
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.DecoratableTx
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.Function1
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.function1

interface UtilityFunctions : DecoratableFunctions

val DecoratableTx.utilityOrNull: UtilityFunctions?
    get() = decorate("Utility") {
        object : UtilityFunctions, DecoratableFunctions by this {}
    }

val DecoratableTx.utility: UtilityFunctions
    get() = utilityOrNull ?: SubstrateApiException.moduleNotFound("Utility")

val UtilityFunctions.batch: Function1<List<GenericCall.Instance>>
    get() = decorator.function1("batch")
