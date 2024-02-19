package io.novasama.substrate_sdk_android.wsrpc.request.runtime.author

import io.novasama.substrate_sdk_android.wsrpc.request.runtime.RuntimeRequest

private const val METHOD = "author_pendingExtrinsics"

class PendingExtrinsicsRequest : RuntimeRequest(METHOD, listOf())
