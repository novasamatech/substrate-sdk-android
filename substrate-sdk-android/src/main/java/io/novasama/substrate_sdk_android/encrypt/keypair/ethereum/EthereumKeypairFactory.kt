package io.novasama.substrate_sdk_android.encrypt.keypair.ethereum

import io.novasama.substrate_sdk_android.encrypt.junction.Junction
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.encrypt.keypair.generate

object EthereumKeypairFactory {

    fun generate(seed: ByteArray, junctions: List<Junction>): Keypair {
        return Bip32KeypairFactory.generate(seed, junctions)
    }
}
