// package io.novasama.substrate_sdk_android.runtime.extrinsic
//
// import io.novasama.substrate_sdk_android.encrypt.SignatureWrapper
// import io.novasama.substrate_sdk_android.runtime.AccountId
// import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
// import io.novasama.substrate_sdk_android.runtime.definitions.types.RuntimeType
// import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.DefaultSignedExtensions
// import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Era
// import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Extrinsic
// import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.ExtrinsicPayloadExtrasInstance
// import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericCall
// import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.new
// import io.novasama.substrate_sdk_android.runtime.definitions.types.instances.AddressInstanceConstructor
// import io.novasama.substrate_sdk_android.runtime.definitions.types.instances.SignatureInstanceConstructor
// import io.novasama.substrate_sdk_android.runtime.extrinsic.signer.SendableExtrinsic
// import io.novasama.substrate_sdk_android.runtime.extrinsic.signer.SignedExtrinsic
// import io.novasama.substrate_sdk_android.runtime.extrinsic.signer.Signer
// import io.novasama.substrate_sdk_android.runtime.extrinsic.signer.SignerPayloadExtrinsic
// import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionId
// import io.novasama.substrate_sdk_android.runtime.metadata.SignedExtensionValue
// import io.novasama.substrate_sdk_android.runtime.metadata.call
// import io.novasama.substrate_sdk_android.runtime.metadata.module
// import io.novasama.substrate_sdk_android.wsrpc.request.runtime.chain.RuntimeVersion
// import java.math.BigInteger
//
// private val DEFAULT_TIP = BigInteger.ZERO
//
// class ExtrinsicBuilder(
//    val runtime: RuntimeSnapshot,
//    private val nonce: Nonce,
//    private val runtimeVersion: RuntimeVersion,
//    private val genesisHash: ByteArray,
//    private val accountId: AccountId,
//    private val signer: Signer,
//    private val blockHash: ByteArray = genesisHash,
//    private val era: Era = Era.Immortal,
//    private val tip: BigInteger = DEFAULT_TIP,
//    private val checkMetadataHash: CheckMetadataHash = CheckMetadataHash.Disabled,
//    customSignedExtensions: Map<TransactionExtensionId, SignedExtensionValue> = emptyMap(),
//    private val addressInstanceConstructor: RuntimeType.InstanceConstructor<AccountId> = AddressInstanceConstructor,
//    private val signatureConstructor: RuntimeType.InstanceConstructor<SignatureWrapper> = SignatureInstanceConstructor
// ) {
//
//    private val mutableCalls = mutableListOf<GenericCall.Instance>()
//
//    private val _customSignedExtensions = mutableMapOf<TransactionExtensionId, SignedExtensionValue>()
//        .apply { putAll(customSignedExtensions) }
//
//    fun call(
//        moduleIndex: Int,
//        callIndex: Int,
//        args: Map<String, Any?>
//    ): ExtrinsicBuilder {
//        val module = runtime.metadata.module(moduleIndex)
//        val function = module.call(callIndex)
//
//        mutableCalls.add(GenericCall.Instance(module, function, args))
//
//        return this
//    }
//
//    fun call(
//        moduleName: String,
//        callName: String,
//        arguments: Map<String, Any?>
//    ): ExtrinsicBuilder {
//        val module = runtime.metadata.module(moduleName)
//        val function = module.call(callName)
//
//        mutableCalls.add(GenericCall.Instance(module, function, arguments))
//
//        return this
//    }
//
//    fun call(call: GenericCall.Instance): ExtrinsicBuilder {
//        mutableCalls.add(call)
//
//        return this
//    }
//
//    fun calls(calls: List<GenericCall.Instance>): ExtrinsicBuilder {
//        mutableCalls.addAll(calls)
//
//        return this
//    }
//
//    fun signedExtension(
//        id: TransactionExtensionId,
//        value: SignedExtensionValue
//    ) {
//        _customSignedExtensions[id] = value
//    }
//
//    fun resetCalls(): ExtrinsicBuilder {
//        mutableCalls.clear()
//
//        return this
//    }
//
//    fun getWrappedCall(
//        batchMode: BatchMode = BatchMode.BATCH
//    ): GenericCall.Instance {
//        return maybeWrapInBatch(batchMode)
//    }
//
//    fun getCalls(): List<GenericCall.Instance> {
//        return takeCallSnapshot()
//    }
//
//    suspend fun buildExtrinsic(
//        batchMode: BatchMode = BatchMode.BATCH
//    ): SendableExtrinsic {
//        val call = maybeWrapInBatch(batchMode)
//        return buildSendableExtrinsic(call)
//    }
//
//    private fun maybeWrapInBatch(batchMode: BatchMode): GenericCall.Instance {
//        val calls = takeCallSnapshot()
//
//        return if (calls.size == 1) {
//            calls.first()
//        } else {
//            wrapInBatch(calls, batchMode)
//        }
//    }
//
//    private suspend fun buildSendableExtrinsic(call: GenericCall.Instance): SendableExtrinsic {
//        val signerPayload = SignerPayloadExtrinsic(
//            runtime = runtime,
//            accountId = accountId,
//            call = call,
//            signedExtras = SignerPayloadExtrinsic.SignedExtras(
//                includedInExtrinsic = buildIncludedInExtrinsic(),
//                includedInSignature = buildIncludedInSignature()
//
//            ),
//            nonce = nonce
//        )
//
//        val signedExtrinsic = signer.signExtrinsic(signerPayload)
//        val instance = buildEncodingExtrinsic(signedExtrinsic)
//
//        return SendableExtrinsic(runtime, instance)
//    }
//
//    private fun buildEncodingExtrinsic(signedExtrinsic: SignedExtrinsic): Extrinsic.Instance {
//        val address = buildEncodableAddressInstance(signedExtrinsic.payload.accountId)
//        val multiSignature = signatureConstructor.constructInstance(
//            runtime.typeRegistry,
//            signedExtrinsic.signatureWrapper
//        )
//        return Extrinsic.Instance(
//            signature = Extrinsic.Signature.new(
//                accountIdentifier = address,
//                signature = multiSignature,
//                signedExtras = signedExtrinsic.payload.signedExtras.includedInExtrinsic
//            ),
//            call = signedExtrinsic.payload.call
//        )
//    }
//
//    private fun buildIncludedInSignature(): Map<String, Any?> {
//        val default = mapOf(
//            DefaultSignedExtensions.CHECK_MORTALITY to blockHash,
//            DefaultSignedExtensions.CHECK_GENESIS to genesisHash,
//            DefaultSignedExtensions.CHECK_SPEC_VERSION to runtimeVersion.specVersion.toBigInteger(),
//            DefaultSignedExtensions.CHECK_TX_VERSION to
//                runtimeVersion.transactionVersion.toBigInteger(),
//            DefaultSignedExtensions.CHECK_METADATA_HASH to
//                checkMetadataHash.toSignedExtensionValue().includedInSignature
//        )
//
//        val custom = _customSignedExtensions.mapValues { (_, extensionValues) ->
//            extensionValues.includedInSignature
//        }
//
//        return default + custom
//    }
//
//    private fun wrapInBatch(
//        calls: List<GenericCall.Instance>,
//        batchMode: BatchMode
//    ): GenericCall.Instance {
//        val batchModule = runtime.metadata.module("Utility")
//
//        val batchFunctionName = when (batchMode) {
//            BatchMode.BATCH -> "batch"
//            BatchMode.BATCH_ALL -> "batch_all"
//            BatchMode.FORCE_BATCH -> "force_batch"
//        }
//        val batchFunction = batchModule.call(batchFunctionName)
//
//        return GenericCall.Instance(
//            module = batchModule,
//            function = batchFunction,
//            arguments = mapOf(
//                "calls" to calls
//            )
//        )
//    }
//
//    private fun takeCallSnapshot(): List<GenericCall.Instance> {
//        return mutableCalls.toMutableList()
//    }
//
//    private fun buildEncodableAddressInstance(accountId: AccountId): Any? {
//        return addressInstanceConstructor.constructInstance(runtime.typeRegistry, accountId)
//    }
//
//    private fun buildIncludedInExtrinsic(): ExtrinsicPayloadExtrasInstance {
//        val default = mapOf(
//            DefaultSignedExtensions.CHECK_MORTALITY to era,
//            DefaultSignedExtensions.CHECK_TX_PAYMENT to tip,
//            DefaultSignedExtensions.CHECK_NONCE to runtime.encodeNonce(nonce.nonce),
//            DefaultSignedExtensions.CHECK_METADATA_HASH to
//                checkMetadataHash.toSignedExtensionValue().includedInExtrinsic
//        )
//
//        val custom = _customSignedExtensions.mapValues { (_, extensionValues) ->
//            extensionValues.includedInExtrinsic
//        }
//
//        return default + custom
//    }
// }
