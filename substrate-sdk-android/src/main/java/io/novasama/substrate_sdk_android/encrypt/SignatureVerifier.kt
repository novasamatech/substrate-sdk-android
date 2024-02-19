package io.novasama.substrate_sdk_android.encrypt

import io.novasama.substrate_sdk_android.encrypt.keypair.ECDSAUtils
import io.novasama.substrate_sdk_android.extensions.fromHex
import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.EdDSASecurityProvider
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import org.web3j.crypto.Sign
import java.security.Signature

object SignatureVerifier {

    fun verify(
        signatureWrapper: SignatureWrapper,
        messageHashing: Signer.MessageHashing,
        data: ByteArray,
        publicKey: ByteArray
    ): Boolean {
        return when (signatureWrapper) {
            is SignatureWrapper.Ecdsa -> {
                verifyEcdsa(signatureWrapper, data, publicKey, messageHashing)
            }

            is SignatureWrapper.Ed25519 -> {
                verifyEd25519(signatureWrapper.signature, data, publicKey)
            }

            is SignatureWrapper.Sr25519 -> {
                verifySr25519(signatureWrapper.signature, data, publicKey)
            }
        }
    }

    private fun verifyEcdsa(
        signature: SignatureWrapper.Ecdsa,
        message: ByteArray,
        publicKeyBytes: ByteArray,
        messageHashing: Signer.MessageHashing,
    ): Boolean = runCatching {
        val signatureData = Sign.SignatureData(signature.v, signature.r, signature.s)

        val hashedMessage = messageHashing.hasher(message)

        val publicKeyInt = Sign.signedMessageHashToKey(hashedMessage, signatureData)
        val signaturePublicKey = publicKeyInt.toString(16).fromHex()

        val decompressedExpectedPublicKey = ECDSAUtils.decompressed(publicKeyBytes)

        signaturePublicKey.contentEquals(decompressedExpectedPublicKey)
    }.getOrDefault(false)

    private fun verifyEd25519(
        signature: ByteArray,
        message: ByteArray,
        publicKeyBytes: ByteArray
    ): Boolean {
        val spec: EdDSAParameterSpec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
        val sgr: Signature = Signature.getInstance(
            EdDSAEngine.SIGNATURE_ALGORITHM,
            EdDSASecurityProvider.PROVIDER_NAME
        )

        val privKeySpec = EdDSAPublicKeySpec(publicKeyBytes, spec)
        val publicKey = EdDSAPublicKey(privKeySpec)
        sgr.initVerify(publicKey)
        sgr.update(message)

        return sgr.verify(signature)
    }

    private fun verifySr25519(
        signature: ByteArray,
        message: ByteArray,
        publicKeyBytes: ByteArray
    ): Boolean {
        return Sr25519.verify(signature, message, publicKeyBytes)
    }
}
