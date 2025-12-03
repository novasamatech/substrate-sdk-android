package io.novasama.substrate_sdk_android.encrypt

import io.novasama.substrate_sdk_android.TestData
import io.novasama.substrate_sdk_android.encrypt.Signer.MessageHashing
import io.novasama.substrate_sdk_android.encrypt.keypair.substrate.ECDSASubstrateKeypairFactory
import io.novasama.substrate_sdk_android.encrypt.keypair.substrate.Ed25519SubstrateKeypairFactory
import io.novasama.substrate_sdk_android.encrypt.keypair.substrate.Sr25519SubstrateKeypairFactory
import org.junit.Assert.assertEquals
import org.junit.Test

private val MESSAGE = "Test message".encodeToByteArray()
private val WRONG_MESSAGE = "Wrong message".encodeToByteArray()
private val WRONG_SEED = ByteArray(32) { 1 }
private val WRONG_ENCRYPTED_KEY = ByteArray(64) { 1 }

fun interface SignTest {

    fun run(signSecret: ByteArray, verifySecret: ByteArray, signMessage: ByteArray, verifyMessage: ByteArray, shouldBeValid: Boolean)
}

class SignatureVerifierTest {

    @Test
    fun `should verify ECDSA`() {
        val isSubstrateOptions = listOf(true, false)

        isSubstrateOptions.forEach { isSubstrate ->
            runSignTests(
                validSecret = TestData.SEED_BYTES,
                invalidSecret = WRONG_SEED
            ) { signSeed, verifySeed, signMessage, verifyMessage, shouldBeValid ->
                signAndVerifyEcdsa(
                    isSubstrate = isSubstrate,
                    signSeed = signSeed,
                    verifySeed = verifySeed,
                    signMessage = signMessage,
                    verifyMessage = verifyMessage,
                    shouldBeValid = shouldBeValid
                )
            }
        }
    }

    @Test
    fun `should verify Ed25519`() {
        runSignTests(
            validSecret = TestData.SEED_BYTES,
            invalidSecret = WRONG_SEED,
            ::signAndVerifyEd25519
        )
    }

    @Test
    fun `should verify sr25519`() {
        runSignTests(
            validSecret = TestData.SEED_BYTES,
            invalidSecret = WRONG_SEED,
            ::signAndVerifySr25519
        )
    }

    @Test
    fun `should verify sr25519 encrypted key`() {
        runSignTests(
            validSecret = TestData.ENCRYPTED_KEY_BYTES,
            invalidSecret = WRONG_ENCRYPTED_KEY,
            ::signAndVerifySr25519WithEncryptedKey
        )
    }

    private fun signAndVerifySr25519(
        signSeed: ByteArray,
        verifySeed: ByteArray,
        signMessage: ByteArray,
        verifyMessage: ByteArray,
        shouldBeValid: Boolean,
    ) {
        val signKeypair = Sr25519SubstrateKeypairFactory.deriveFromSeed(signSeed)
        val verifyKeypair = Sr25519SubstrateKeypairFactory.deriveFromSeed(verifySeed)

        val signature = Signer.sign(
            multiChainEncryption = MultiChainEncryption.Substrate(EncryptionType.SR25519),
            message = signMessage,
            keypair = signKeypair
        )
        val isValid = SignatureVerifier.verify(
            signatureWrapper = signature,
            messageHashing = MessageHashing.SUBSTRATE,
            data = verifyMessage,
            publicKey = verifyKeypair.publicKey
        )

        assertEquals(shouldBeValid, isValid)
    }

    private fun signAndVerifySr25519WithEncryptedKey(
        signKey: ByteArray,
        verifyKey: ByteArray,
        signMessage: ByteArray,
        verifyMessage: ByteArray,
        shouldBeValid: Boolean,
    ) {
        val signKeypair = Sr25519SubstrateKeypairFactory.deriveEncryptedKeypair(signKey)
        val verifyKeypair = Sr25519SubstrateKeypairFactory.deriveEncryptedKeypair(verifyKey)

        val signature = Signer.sign(
            multiChainEncryption = MultiChainEncryption.Substrate(EncryptionType.SR25519),
            message = signMessage,
            keypair = signKeypair
        )
        val isValid = SignatureVerifier.verify(
            signatureWrapper = signature,
            messageHashing = MessageHashing.SUBSTRATE,
            data = verifyMessage,
            publicKey = verifyKeypair.publicKey
        )

        assertEquals(shouldBeValid, isValid)
    }

    private fun signAndVerifyEd25519(
        signSeed: ByteArray,
        verifySeed: ByteArray,
        signMessage: ByteArray,
        verifyMessage: ByteArray,
        shouldBeValid: Boolean,
    ) {
        val signKeypair = Ed25519SubstrateKeypairFactory.deriveFromSeed(signSeed)
        val verifyKeypair = Ed25519SubstrateKeypairFactory.deriveFromSeed(verifySeed)

        val signature = Signer.sign(
            multiChainEncryption = MultiChainEncryption.Substrate(EncryptionType.ED25519),
            message = signMessage,
            keypair = signKeypair
        )
        val isValid = SignatureVerifier.verify(
            signatureWrapper = signature,
            messageHashing = MessageHashing.SUBSTRATE,
            data = verifyMessage,
            publicKey = verifyKeypair.publicKey
        )

        assertEquals(shouldBeValid, isValid)
    }

    private fun signAndVerifyEcdsa(
        signSeed: ByteArray,
        verifySeed: ByteArray,
        signMessage: ByteArray,
        verifyMessage: ByteArray,
        isSubstrate: Boolean,
        shouldBeValid: Boolean,
    ) {
        val signKeypair = ECDSASubstrateKeypairFactory.deriveFromSeed(signSeed)
        val verifyKeypair = ECDSASubstrateKeypairFactory.deriveFromSeed(verifySeed)

        val multiChainEncryption = if (isSubstrate) {
            MultiChainEncryption.Substrate(EncryptionType.ECDSA)
        } else {
            MultiChainEncryption.Ethereum
        }

        val messageHashing = if (isSubstrate) {
            MessageHashing.SUBSTRATE
        } else {
            MessageHashing.ETHEREUM
        }

        val signature = Signer.sign(
            multiChainEncryption = multiChainEncryption,
            message = signMessage,
            keypair = signKeypair
        )
        val isValid = SignatureVerifier.verify(
            signatureWrapper = signature,
            messageHashing = messageHashing,
            data = verifyMessage,
            publicKey = verifyKeypair.publicKey
        )

        assertEquals(shouldBeValid, isValid)
    }

    private fun runSignTests(
        validSecret: ByteArray,
        invalidSecret: ByteArray,
        test: SignTest
    ) {
        test.run(
            signSecret = validSecret,
            verifySecret = validSecret,
            signMessage = MESSAGE,
            verifyMessage = MESSAGE,
            shouldBeValid = true
        )

        test.run(
            signSecret = validSecret,
            verifySecret = invalidSecret,
            signMessage = MESSAGE,
            verifyMessage = MESSAGE,
            shouldBeValid = false
        )

        test.run(
            signSecret = validSecret,
            verifySecret = validSecret,
            signMessage = MESSAGE,
            verifyMessage = WRONG_MESSAGE,
            shouldBeValid = false
        )
    }
}