package io.novasama.substrate_sdk_android.encrypt.json

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import org.junit.Test

class Sr25519JsonSeedEncoderTest : BaseSubstrateJsonEncoderTest() {

    override val encryptionType = EncryptionType.SR25519

    @Test
    fun `encode should be compatible with decode without derivation path`() {
        testSelfCompatibility(derivationPath = null)
    }

    @Test
    fun `encode should be compatible with decode with derivation path`() {
        testSelfCompatibility(derivationPath = "//1//2")
    }

    @Test
    fun `decode should be compatible with external sources without derivation path`() {
        testExternalCompatibility(
            derivationPath = null,
            json = """{"encoded":"jmYHIrtqukPPz9E02Tu+aiVZD6xc6APktjpgYZJ24bIAAAIAAQAAAAgAAAD7mRHDCPMBNJ8sWiek4TQhry+YeHj4gZICVWIryZKjSyzChNHl46M32mbyL6GeEwj3GRofvgL1kPSRRRpDZTLqYm1oY03WRRwUejU4iL1/YVp0MJIVRZMebTRADrqLZ+a5ZNZtHUQjv1Uh9J31zY2ZETuI5b2MmAMdgO930AFRBpDD8s11dDiMJiepZ82XsrPvpJ6AebT3sRwuGcMp","encoding":{"content":["pkcs8","sr25519"],"type":["scrypt","xsalsa20-poly1305"],"version":"3"},"address":"1WPNKpkYd8YoaXEgJm4wfaFEAcHiZ6WSxvZzj9P8cK7z62d","meta":{"genesisHash":"0x91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3","isHardware":false,"name":"test","tags":[],"whenCreated":1754995216570}}"""
        )
    }

    @Test
    fun `decode should be compatible with external sources with derivation path`() {
        testExternalCompatibility(
            derivationPath = "//1//2",
            json = """{"encoded":"9Kz0aVe+usJB1E9DFiuH5Rn1jMUaacjLAh6yVefYR94AAAIAAQAAAAgAAADnFBscCEneV4WJduHfPPYJJ7+ansAvtpk6BbbOkMbjp+JA1mWoLXpZKyAUo96eAXqJ9XAPv39WYta0bQvi1agw4DJPmDQGhgUiKJTBPjXBQ6vj8uDIo+T3eFtlrQMmbn49OCVQ0lTQPJTNcLpblhmHCx1Ybs+jQpOjHZjO+MU3xJgc9WROkCwnAYUwKYK0sbw/Z6xiT5Q0HiM92jku","encoding":{"content":["pkcs8","sr25519"],"type":["scrypt","xsalsa20-poly1305"],"version":"3"},"address":"153N7DQL8i3X9r4rkFaw2YbsmrqLeChZqkXEVkQpFdSJtHZV","meta":{"genesisHash":"0x91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3","isHardware":false,"name":"test","tags":[],"whenCreated":1754995249679}}"""
        )
    }
}