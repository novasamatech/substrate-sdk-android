package jp.co.soramitsu.fearless_utils.samples.decoratable_api.derive.utility

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericCall
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.DecoratableFunctions
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.DecoratableTx
import jp.co.soramitsu.fearless_utils.decoratable_api.tx.Function1

interface UtilityFunctions : DecoratableFunctions

val DecoratableTx.utility: UtilityFunctions
    get() = decorate("Utility") {
        object : UtilityFunctions, DecoratableFunctions by this {}
    }

val UtilityFunctions.batch: Function1<List<GenericCall.Instance>>
    get() = decorator.function1("batch")
