package io.novasama.substrate_sdk_android.encrypt

import io.novasama.substrate_sdk_android.encrypt.keypair.Keypair
import io.novasama.substrate_sdk_android.encrypt.keypair.substrate.Sr25519Keypair
import io.novasama.substrate_sdk_android.hash.Hasher.blake2b256
import io.novasama.substrate_sdk_android.hash.Hasher.keccak256
import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSASecurityProvider
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec
import org.bouncycastle.util.encoders.Hex
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Sign
import java.math.BigInteger
import java.security.Signature

object Signer {

    enum class MessageHashing(val hasher: (ByteArray) -> ByteArray) {
        SUBSTRATE(hasher = { it.blake2b256() }),
        ETHEREUM(hasher = { it.keccak256() })
    }

    init {
        SecurityProviders.requireEdDSA
        SecurityProviders.requireBouncyCastle
    }

    fun sign(
        multiChainEncryption: MultiChainEncryption,
        message: ByteArray,
        keypair: Keypair,
        skipHashing: Boolean = false
    ): SignatureWrapper {
        return when (multiChainEncryption) {

            is MultiChainEncryption.Ethereum -> {
                signEcdsa(message, keypair, MessageHashing.ETHEREUM.hasher, skipHashing)
            }

            is MultiChainEncryption.Substrate -> {

                when (multiChainEncryption.encryptionType) {

                    EncryptionType.SR25519 -> {
                        require(keypair is Sr25519Keypair) {
                            "Sr25519Keypair is needed to sign with SR25519"
                        }

                        signSr25519(message, keypair)
                    }

                    EncryptionType.ED25519 -> signEd25519(message, keypair)

                    EncryptionType.ECDSA -> signEcdsa(
                        message,
                        keypair,
                        MessageHashing.SUBSTRATE.hasher,
                        skipHashing
                    )
                }
            }
        }
    }

    private fun signSr25519(message: ByteArray, keypair: Sr25519Keypair): SignatureWrapper {
        val sign = io.novasama.substrate_sdk_android.encrypt.Sr25519.sign(keypair.publicKey, keypair.privateKey + keypair.nonce, message)

        return SignatureWrapper.Sr25519(signature = sign)
    }

    private fun signEd25519(message: ByteArray, keypair: Keypair): SignatureWrapper {
        val spec: EdDSAParameterSpec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
        val sgr: Signature = Signature.getInstance(
            EdDSAEngine.SIGNATURE_ALGORITHM,
            EdDSASecurityProvider.PROVIDER_NAME
        )
        val privKeySpec = EdDSAPrivateKeySpec(keypair.privateKey, spec)
        val privateKey = EdDSAPrivateKey(privKeySpec)
        sgr.initSign(privateKey)
        sgr.update(message)
        return SignatureWrapper.Ed25519(signature = sgr.sign())
    }

    private fun signEcdsa(
        message: ByteArray,
        keypair: Keypair,
        hasher: (ByteArray) -> ByteArray,
        skipHashing: Boolean
    ): SignatureWrapper {
        val privateKey = BigInteger(Hex.toHexString(keypair.privateKey), 16)
        val publicKey = Sign.publicKeyFromPrivate(privateKey)

        val messageHash = if (skipHashing) {
            message
        } else {
            hasher(message)
        }

        val sign = Sign.signMessage(messageHash, ECKeyPair(privateKey, publicKey), false)

        return SignatureWrapper.Ecdsa(v = sign.v, r = sign.r, s = sign.s)
    }
}
