package io.novasama.substrate_sdk_android

import io.novasama.substrate_sdk_android.ss58.SS58Encoder.toAccountId
import org.bouncycastle.util.encoders.Hex

object TestData {
    const val PUBLIC_KEY = "2f8c6129d816cf51c374bc7f08c3e63ed156cf78aefb4a6550d97b87997977ee"
    val PUBLIC_KEY_BYTES = Hex.decode(PUBLIC_KEY)

    const val PRIVATE_KEY = "f0106660c3dda23f16daa9ac5b811b963077f5bc0af89f85804f0de8e424f050"
    val PRIVATE_KEY_BYTES = Hex.decode(PRIVATE_KEY)

    // The pair of private key and its address
    const val SUBSTRATE_SECRET_KEY = "c2991f02cfee78ca87987878a13ebd573f6e9bc5f1d7711ef8dc257a741e1a0359018ef1850586cb970a7bd186bd044b3ed22a1732c5be0199048b5e88716fce"
    val SUBSTRATE_SECRET_KEY_BYTES = Hex.decode(SUBSTRATE_SECRET_KEY)
    const val SUBSTRATE_SECRET_ACCOUNT_ADDRESS = "13ieqHQBpGM5N38YVuihrqPDbeTvzs3HYAeevCHcEG6BJ1Ba"
    val SUBSTRATE_SECRET_ACCOUNT_ID = SUBSTRATE_SECRET_ACCOUNT_ADDRESS.toAccountId()

    const val SEED = "3132333435363738393031323334353637383930313233343536373839303132"
    val SEED_BYTES: ByteArray = Hex.decode(SEED)
}