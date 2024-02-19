package io.novasama.substrate_sdk_android.encrypt.keypair.ethereum

import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair

class Bip32ExtendedKeyPair(
    override val privateKey: ByteArray,
    override val publicKey: ByteArray,
    val chaincode: ByteArray
) : Keypair
