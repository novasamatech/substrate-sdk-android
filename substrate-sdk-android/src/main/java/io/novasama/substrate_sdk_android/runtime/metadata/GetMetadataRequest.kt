package io.novasama.substrate_sdk_android.runtime.metadata

import io.novasama.substrate_sdk_android.wsrpc.request.runtime.RuntimeRequest

object GetMetadataRequest : RuntimeRequest("state_getMetadata", listOf())
