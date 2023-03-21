package io.novasama.substrate_sdk_android.wsrpc.request.runtime.chain

import io.novasama.substrate_sdk_android.wsrpc.request.runtime.RuntimeRequest

private const val METHOD = "chain_getRuntimeVersion"

class RuntimeVersionRequest : RuntimeRequest(METHOD, listOf())

class RuntimeVersion(val specVersion: Int, val transactionVersion: Int)
