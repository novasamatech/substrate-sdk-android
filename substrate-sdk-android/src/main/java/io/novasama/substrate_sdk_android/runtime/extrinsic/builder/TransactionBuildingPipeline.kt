package io.novasama.substrate_sdk_android.runtime.extrinsic.builder

import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.Extrinsic
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.GeneralTransactionParams

interface TransactionBuildingPipeline {

    suspend fun constructExtrinsicType(generalTransactionParams: GeneralTransactionParams): Extrinsic.ExtrinsicType
}