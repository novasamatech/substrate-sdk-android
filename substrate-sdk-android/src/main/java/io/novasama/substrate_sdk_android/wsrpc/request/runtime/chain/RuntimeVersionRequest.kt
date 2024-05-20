package io.novasama.substrate_sdk_android.wsrpc.request.runtime.chain

import io.novasama.substrate_sdk_android.wsrpc.request.runtime.RuntimeRequest

private const val METHOD = "state_getRuntimeVersion"

class RuntimeVersionRequest : RuntimeRequest(METHOD, listOf())

open class RuntimeVersion(val specVersion: Int, val transactionVersion: Int)

class RuntimeVersionFull(specVersion: Int, transactionVersion: Int, val specName: String) : RuntimeVersion(specVersion, transactionVersion)
