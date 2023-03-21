package io.novasama.substrate_sdk_android.extensions

import org.junit.Assert.assertEquals
import org.junit.Test

class EthereumKtTest {

    private val validChecksumAddresses = listOf(
        // All caps
        "0x52908400098527886E0F7030069857D2E4169EE7",
        "0x8617E340B3D01FA5F11F306F4090FD50E238070D",
        // All Lower
        "0xde709f2102306220921060314715629080e2fb77",
        "0x27b1fdb04752bbc536007a920d24acb045561c26",
        // Normal
        "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed",
        "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359",
        "0xdbF03B407c01E7cD3CBea99509d93f8DDDC8C6FB",
        "0xD1220A0cf47c7B9Be7A2E6BA89F429762e7b9aDb",
    )

    private val validNoChecksumAddresses = listOf(
        "0x52908400098527886E0F7030069857D2E4169EE7"
    )

    private val invalidEthereumAddresses = listOf(
        "0x0123", // too short
        "0x12345678901234567890123456789012345678901", // too long
        "0xQ234567890123456789012345678901234567890", // has invalid character (Q)
        "0x52908400098527886E0F7030069857D2E4169ee7" // wrong checksum
    )

    @Test
    fun `should generate address with checksum`() {
        validChecksumAddresses.forEach { address ->
            val accountId = Ethereum.Address(address).toAccountId()
            val actualAddress = accountId.toAddress(withChecksum = true)

            assertEquals(address, actualAddress.value)
        }
    }

    @Test
    fun `should check if address is valid`() {
        val cases = validChecksumAddresses.map { it to true } +
                validNoChecksumAddresses.map { it to true } +
                invalidEthereumAddresses.map { it to false }

        cases.forEach { (address, isValid) ->
            assertEquals(isValid, Ethereum.Address(address).isValid())
        }
    }

    @Test
    fun `should generate correct ethereum address`() {
        val publicKey =
            "6e145ccef1033dea239875dd00dfb4fee6e3348b84985c92f103444683bae07b83b5c38e5e2b0c8529d7fa3f64d46daa1ece2d9ac14cab9477d042c84c32ccd0"
                .fromHex()
                .asEthereumPublicKey()

        val expectedNoChecksum = Ethereum.Address("0x001d3f1ef827552ae1114027bd3ecf1f086ba0f9")
        assertEquals(expectedNoChecksum, publicKey.toAddress(withChecksum = false))

        val expectedWithChecksum = Ethereum.Address("0x001d3F1ef827552Ae1114027BD3ECF1f086bA0F9")
        assertEquals(expectedWithChecksum, publicKey.toAddress(withChecksum = true))
    }


}