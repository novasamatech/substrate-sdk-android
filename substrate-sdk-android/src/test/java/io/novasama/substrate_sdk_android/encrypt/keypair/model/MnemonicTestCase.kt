package io.novasama.substrate_sdk_android.encrypt.keypair.model

import com.google.gson.annotations.SerializedName

data class MnemonicTestCase(
    val mnemonic: String,
    val path: String,
    @SerializedName("pk")
    val expectedPublicKey: String,
)