package io.novasama.substrate_sdk_android.encrypt.keypair.bip32

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.novasama.substrate_sdk_android.common.assertHexEquals
import io.novasama.substrate_sdk_android.extensions.fromHex
import io.novasama.substrate_sdk_android.getResourceReader
import org.junit.Test

class Bip32Ed25519KeypairFactoryTest {

    private val gson = Gson()

    @Test(expected = IllegalArgumentException::class)
    fun `test BIP32 SLIP-0010 soft derivation throws exception`() {
        val seed = ByteArray(32) { it.toByte() }
        Bip32Ed25519KeypairFactory.generate(seed, "/0")
    }

    // SLIP-0010 Official Test Vectors: https://github.com/satoshilabs/slips/blob/master/slip-0010.md
    @Test
    fun `test SLIP-0010 Ed25519 test vectors from JSON`() {
        val stream = getResourceReader("crypto/bip32_ed25519_test_vectors.json")
        val testVectorData = gson.fromJson(stream, TestVectorData::class.java)

        testVectorData.testVectors.forEach { testVector ->
            val seed = testVector.seed.fromHex()

            val expectedPrivateKey = testVector.privateKey.fromHex()
            val expectedPublicKey = testVector.publicKey.fromHex()

            val actual = Bip32Ed25519KeypairFactory.generate(seed, testVector.derivationPath)

            assertHexEquals(expectedPrivateKey, actual.privateKey)
            assertHexEquals(expectedPublicKey, actual.publicKey)
        }
    }

    private data class TestVectorData(
        @SerializedName("test_vectors") val testVectors: List<TestVector>
    )

    private data class TestVector(
        val name: String,
        val seed: String,
        @SerializedName("private_key") val privateKey: String,
        @SerializedName("public_key") val publicKeyWithPrefix: String,
        @SerializedName("derivation_path") val derivationPath: String?
    ) {

        val publicKey: String
            // drop "00" prefix which is present in slip10 vectors
            // It is present in the test vectors ensure unified format with other vectors
            // But we do not need it
            get() = publicKeyWithPrefix.drop(2)
    }
}
