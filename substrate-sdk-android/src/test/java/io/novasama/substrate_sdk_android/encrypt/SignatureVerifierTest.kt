package io.novasama.substrate_sdk_android.encrypt

import io.novasama.substrate_sdk_android.TestData
import io.novasama.substrate_sdk_android.encrypt.Signer.MessageHashing
import io.novasama.substrate_sdk_android.encrypt.keypair.substrate.ECDSASubstrateKeypairFactory
import io.novasama.substrate_sdk_android.encrypt.keypair.substrate.Ed25519SubstrateKeypairFactory
import io.novasama.substrate_sdk_android.encrypt.keypair.substrate.Sr25519SubstrateKeypairFactory
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test

private val MESSAGE = "Test message".encodeToByteArray()
private val WRONG_MESSAGE = "Wrong message".encodeToByteArray()
private val WRONG_SEED = ByteArray(32) { 1 }

fun interface SignTest {

    fun run(signSeed: ByteArray, verifySeed: ByteArray, signMessage: ByteArray, verifyMessage: ByteArray, shouldBeValid: Boolean)
}

class SignatureVerifierTest {

    @Test
    fun `should verify ECDSA`() {
        val isSubstrateOptions = listOf(true, false)

        isSubstrateOptions.forEach { isSubstrate ->
            runSignTests { signSeed, verifySeed, signMessage, verifyMessage, shouldBeValid ->
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
        runSignTests(::signAndVerifyEd25519)
    }

    @Test
    @Ignore("TODO - make Sr25519 .dylib loading work on Linux/Macos")
    fun `should verify sr25519`() {
        runSignTests(::signAndVerifySr25519)
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
        test: SignTest
    ) {
        test.run(
            signSeed = TestData.SEED_BYTES,
            verifySeed = TestData.SEED_BYTES,
            signMessage = MESSAGE,
            verifyMessage = MESSAGE,
            shouldBeValid = true
        )

        test.run(
            signSeed = TestData.SEED_BYTES,
            verifySeed = WRONG_SEED,
            signMessage = MESSAGE,
            verifyMessage = MESSAGE,
            shouldBeValid = false
        )

        test.run(
            signSeed = TestData.SEED_BYTES,
            verifySeed = TestData.SEED_BYTES,
            signMessage = MESSAGE,
            verifyMessage = WRONG_MESSAGE,
            shouldBeValid = false
        )
    }
}