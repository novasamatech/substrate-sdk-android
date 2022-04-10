package io.github.nova_wallet.substrate_sdk_android.codegen.common

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.RuntimeType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.isFullyResolved
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object TypeFormatting {

    fun unknownType(parent: String, child: String): Nothing {
        throw IllegalArgumentException("Unknown type in $parent::$child")
    }

    infix fun String.joinTypeName(child: String) = "$this.$child"
}

@OptIn(ExperimentalContracts::class)
fun RuntimeType<*, *>?.requireResolved(parentLabel: String, childLabel: String) {
    contract {
        returns() implies (this@requireResolved != null)
    }

    if (!isFullyResolved()) TypeFormatting.unknownType(parentLabel, childLabel)
}
