package jp.co.soramitsu.fearless_utils.keyring.keypair

import com.google.gson.annotations.SerializedName

data class SeedTestCase(
    val seed: String,
    val path: String,
    @SerializedName("pk")
    val expectedPublicKey: String,
)
