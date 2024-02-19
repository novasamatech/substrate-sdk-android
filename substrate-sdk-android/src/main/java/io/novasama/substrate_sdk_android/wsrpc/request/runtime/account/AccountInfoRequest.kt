package io.novasama.substrate_sdk_android.wsrpc.request.runtime.account

import io.novasama.substrate_sdk_android.runtime.Module
import io.novasama.substrate_sdk_android.wsrpc.request.runtime.storage.GetStorageRequest

class AccountInfoRequest(publicKey: ByteArray) : GetStorageRequest(
    listOf(
        Module.System.Account.storageKey(publicKey)
    )
)
