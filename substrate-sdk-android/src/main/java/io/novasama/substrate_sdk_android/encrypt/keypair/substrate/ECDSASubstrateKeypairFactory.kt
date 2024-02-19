package io.novasama.substrate_sdk_android.encrypt.keypair.substrate

import io.novasama.substrate_sdk_android.encrypt.keypair.ECDSAUtils
import io.novasama.substrate_sdk_android.encrypt.keypair.derivePublicKey

internal object ECDSASubstrateKeypairFactory : OtherSubstrateKeypairFactory("Secp256k1HDKD") {

    override fun deriveFromSeed(seed: ByteArray): KeypairWithSeed {
        return KeypairWithSeed(
            seed = seed,
            privateKey = seed,
            publicKey = ECDSAUtils.derivePublicKey(privateKeyOrSeed = seed)
        )
    }
}
