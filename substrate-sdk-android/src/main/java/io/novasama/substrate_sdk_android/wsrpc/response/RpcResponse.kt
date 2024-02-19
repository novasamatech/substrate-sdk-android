package io.novasama.substrate_sdk_android.wsrpc.response

import com.google.gson.annotations.SerializedName

class RpcResponse(
    @SerializedName("jsonrpc")
    val jsonrpc: String,
    @SerializedName("result")
    val result: Any?,
    @SerializedName("id")
    val id: Int,
    val error: RpcError?
) {
    override fun toString(): String = "RpcResponse($id)"
}

class RpcError(val code: Int, val message: String)
