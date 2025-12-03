package io.novasama.substrate_sdk_android.encrypt.keypair

import io.novasama.substrate_sdk_android.TestData.SUBSTRATE_SECRET_ACCOUNT_ID
import io.novasama.substrate_sdk_android.TestData.SUBSTRATE_SECRET_KEY_BYTES
import io.novasama.substrate_sdk_android.encrypt.keypair.substrate.Sr25519SubstrateKeypairFactory
import io.novasama.substrate_sdk_android.ss58.SS58Encoder.publicKeyToSubstrateAccountId
import org.junit.Assert.assertEquals
import org.junit.Test

class SubstrateKeypairSecretTest {

    @Test
    fun decodedPublicKetIsValid() {
        val keypair = Sr25519SubstrateKeypairFactory.createKeypairFromSecret(SUBSTRATE_SECRET_KEY_BYTES)
        val publicKey = keypair.publicKey
        val accountId = publicKey.publicKeyToSubstrateAccountId()

        assertEquals(accountId.toHexString(), SUBSTRATE_SECRET_ACCOUNT_ID.toHexString())
    }

    @Test
    fun decodedKeyPairIsValid() {
        val rawSecret = SUBSTRATE_SECRET_KEY_BYTES
        val privateKey = rawSecret.copyOfRange(0, 32)
        val nonce = rawSecret.copyOfRange(32, 64)

        val decodedKeyPair = Sr25519SubstrateKeypairFactory.createKeypairFromSecret(SUBSTRATE_SECRET_KEY_BYTES)

        assertEquals(privateKey.toHexString(), decodedKeyPair.privateKey.toHexString())
        assertEquals(nonce.toHexString(), decodedKeyPair.nonce.toHexString())
    }
}

