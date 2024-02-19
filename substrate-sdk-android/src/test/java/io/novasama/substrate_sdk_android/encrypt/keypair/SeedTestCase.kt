package io.novasama.substrate_sdk_android.encrypt.keypair

import com.google.gson.annotations.SerializedName

data class SeedTestCase(
    val seed: String,
    val path: String,
    @SerializedName("pk")
    val expectedPublicKey: String,
)