package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions

import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.DefaultSignedExtensions
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension
import java.math.BigInteger

fun CheckSpecVersion(specVersion: Int) = TransactionExtension(
    name = DefaultSignedExtensions.CHECK_SPEC_VERSION,
    implicit = specVersion.toBigInteger(),
    explicit = null
)