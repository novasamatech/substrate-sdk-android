package io.novasama.substrate_sdk_android.encrypt.keypair.bip32

import io.novasama.substrate_sdk_android.encrypt.junction.BIP32JunctionDecoder
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.encrypt.keypair.KeypairFactory
import io.novasama.substrate_sdk_android.encrypt.keypair.generate

abstract class Bip32KeypairFactory : KeypairFactory<Bip32ExtendedKeyPair>

fun Bip32KeypairFactory.generate(seed: ByteArray, derivationPath: String?): Keypair {
    if (derivationPath == null) return deriveFromSeed(seed)

    val decodedPath = BIP32JunctionDecoder.decode(derivationPath)

    return generate(seed, decodedPath.junctions)
}