package io.novasama.substrate_sdk_android.encrypt.keypair.substrate

import io.novasama.substrate_sdk_android.encrypt.junction.Junction
import io.novasama.substrate_sdk_android.encrypt.junction.JunctionType
import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.encrypt.keypair.KeypairFactory
import io.novasama.substrate_sdk_android.hash.Hasher.blake2b256
import io.novasama.substrate_sdk_android.scale.dataType.string
import io.novasama.substrate_sdk_android.scale.dataType.toByteArray

class KeypairWithSeed(
    val seed: ByteArray,
    override val privateKey: ByteArray,
    override val publicKey: ByteArray
) : Keypair

abstract class OtherSubstrateKeypairFactory(
    private val hardDerivationPrefix: String
) : KeypairFactory<KeypairWithSeed> {

    override fun deriveChild(parent: KeypairWithSeed, junction: Junction): KeypairWithSeed {
        if (junction.type == JunctionType.HARD) {
            val prefix = string.toByteArray(hardDerivationPrefix)

            val newSeed = (prefix + parent.seed + junction.chaincode).blake2b256()

            return deriveFromSeed(newSeed)
        } else {
            throw KeypairFactory.SoftDerivationNotSupported()
        }
    }
}
