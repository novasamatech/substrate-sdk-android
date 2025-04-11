package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions

import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.extrinsic.ExtrinsicVersion
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.InheritedImplication
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension

open class FixedValueTransactionExtension(
    override val name: String,
    private val implicit: Any?,
    private val explicit: Any?,
) : TransactionExtension {

    override suspend fun implicit(): Any? {
        return implicit
    }

    override suspend fun explicit(
        inheritedImplication: InheritedImplication,
        runtimeSnapshot: RuntimeSnapshot,
    ): Any? {
        return explicit
    }
}
