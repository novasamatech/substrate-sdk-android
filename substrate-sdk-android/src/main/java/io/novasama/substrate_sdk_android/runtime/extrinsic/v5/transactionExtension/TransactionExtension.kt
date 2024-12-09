package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension

import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.extrinsic.ExtrinsicVersion
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.FixedValueTransactionExtension

interface TransactionExtension {

    val name: String

    suspend fun implicit(): Any?

    suspend fun explicit(
        inheritedImplication: InheritedImplication,
        extrinsicVersion: ExtrinsicVersion,
        runtimeSnapshot: RuntimeSnapshot,
    ): Any?
}

fun TransactionExtension(
    name: String,
    implicit: Any?,
    explicit: Any?
): TransactionExtension {
    return FixedValueTransactionExtension(name = name, implicit = implicit, explicit = explicit)
}

fun AbsentExtension(name: String) = TransactionExtension(name, null, null)
