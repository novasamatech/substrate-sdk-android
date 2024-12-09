package io.novasama.substrate_sdk_android.runtime.extrinsic

import io.novasama.substrate_sdk_android.runtime.AccountId
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Era
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericCall
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.ChargeTransactionPayment
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.CheckGenesis
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.CheckMortality
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.CheckNonce
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.CheckSpecVersion
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.CheckTxVersion
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.checkMetadataHash.CheckMetadataHash
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.checkMetadataHash.CheckMetadataHashMode
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature.GeneralTransactionSigner
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature.VerifySignature
import io.novasama.substrate_sdk_android.runtime.metadata.call
import io.novasama.substrate_sdk_android.runtime.metadata.module
import io.novasama.substrate_sdk_android.ss58.SS58Encoder.publicKeyToSubstrateAccountId
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.chain.RuntimeVersion
import java.math.BigInteger


fun ExtrinsicBuilder.call(
    moduleIndex: Int,
    callIndex: Int,
    arguments: Map<String, Any?>
): ExtrinsicBuilder {
    val module = runtime.metadata.module(moduleIndex)
    val function = module.call(callIndex)

    val call = GenericCall.Instance(module, function, arguments)

    return call(call)
}

fun ExtrinsicBuilder.call(
    moduleName: String,
    callName: String,
    arguments: Map<String, Any?>
): ExtrinsicBuilder {
    val module = runtime.metadata.module(moduleName)
    val function = module.call(callName)

    val call = GenericCall.Instance(module, function, arguments)

    return call(call)
}

private val DEFAULT_TIP = BigInteger.ZERO


fun ExtrinsicBuilder(
    runtime: RuntimeSnapshot,
    nonce: Nonce,
    runtimeVersion: RuntimeVersion,
    genesisHash: ByteArray,
    accountId: AccountId,
    signer: GeneralTransactionSigner,
    blockHash: ByteArray = genesisHash,
    era: Era = Era.Immortal,
    tip: BigInteger = DEFAULT_TIP,
    checkMetadataHash: CheckMetadataHashMode = CheckMetadataHashMode.Disabled,
    extrinsicVersion: ExtrinsicVersion = ExtrinsicVersion.V4,
    batchMode: BatchMode = BatchMode.BATCH,
): ExtrinsicBuilder {
    return ExtrinsicBuilder(runtime, extrinsicVersion, batchMode).apply {
        setTransactionExtension(VerifySignature.enabled(signer, accountId))
        setTransactionExtension(CheckNonce(nonce))
        setTransactionExtension(CheckMortality(era, blockHash))
        setTransactionExtension(CheckGenesis(genesisHash))
        setTransactionExtension(ChargeTransactionPayment(tip))
        setTransactionExtension(CheckMetadataHash(checkMetadataHash))
        setTransactionExtension(CheckSpecVersion(runtimeVersion.specVersion))
        setTransactionExtension(CheckTxVersion(runtimeVersion.transactionVersion))
    }
}