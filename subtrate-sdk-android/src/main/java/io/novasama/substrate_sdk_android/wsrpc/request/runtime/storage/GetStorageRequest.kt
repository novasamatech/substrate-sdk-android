package io.novasama.substrate_sdk_android.wsrpc.request.runtime.storage

import io.novasama.substrate_sdk_android.wsrpc.request.runtime.RuntimeRequest

open class GetStorageRequest(keys: List<String>) : RuntimeRequest(
    method = "state_getStorage",
    keys
)
