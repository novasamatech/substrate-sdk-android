package io.novasama.substrate_sdk_android.wsrpc.request.runtime.author

import io.novasama.substrate_sdk_android.wsrpc.request.runtime.RuntimeRequest

class SubmitExtrinsicRequest(extrinsic: String) : RuntimeRequest(
    method = "author_submitExtrinsic",
    params = listOf(extrinsic)
)

class SubmitAndWatchExtrinsicRequest(extrinsic: String) : RuntimeRequest(
    method = "author_submitAndWatchExtrinsic",
    params = listOf(extrinsic)
)
