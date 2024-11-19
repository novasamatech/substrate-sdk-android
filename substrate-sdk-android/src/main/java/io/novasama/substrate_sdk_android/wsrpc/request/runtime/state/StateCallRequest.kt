package io.novasama.substrate_sdk_android.wsrpc.request.runtime.state

import io.novasama.substrate_sdk_android.wsrpc.request.runtime.RuntimeRequest

class StateCallRequest(
    runtimeRpcName: String,
    encodedArguments: String
) : RuntimeRequest(
    "state_call",
    listOf(runtimeRpcName, encodedArguments)
)
