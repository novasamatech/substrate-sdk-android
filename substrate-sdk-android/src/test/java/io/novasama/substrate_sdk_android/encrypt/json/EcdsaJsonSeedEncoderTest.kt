package io.novasama.substrate_sdk_android.encrypt.json

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import org.junit.Test

class EcdsaJsonSeedEncoderTest : BaseSubstrateJsonEncoderTest() {

    override val encryptionType = EncryptionType.ECDSA

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
            json = """{"encoded":"5Wz+K2O3NDNMxpikZFaxdohW8WXw1RWXXLacQ1K9zUEAAAIAAQAAAAgAAAACUawDdWhw2pNA8JrQuniFAIXlzMAdGbLrlOOaS1d4aZ4GL70lYrO89+in01BxIKKN/DM5OokiX2yeAq1Ppewdg/7VrXXDbYhDNRPVl19qK7KSs1o7JRYhYpG1bDpNjNJ8djd8xxS+hy+osCboUO+HMnD+RNch52P7Oj8vA38=","encoding":{"content":["pkcs8","ecdsa"],"type":["scrypt","xsalsa20-poly1305"],"version":"3"},"address":"0x03330187607e5d76b412080a19475bb10adaa3204a5bfc7a1115b19d8ba1a54785","meta":{"genesisHash":"0x91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3","isHardware":false,"name":"test","tags":[],"whenCreated":1754995107212}}"""
        )
    }

    @Test
    fun `decode should be compatible with external sources with derivation path`() {
        testExternalCompatibility(
            derivationPath = "//1//2",
            json = """{"encoded":"MUiJU4i+OWVTJdDAOoaTGQTLBrY80me1uKdpNDGYnFAAAAIAAQAAAAgAAAD+PU8Kn1GJBFaIRwGLsoSn+0SRdFdkaovAn3ghE9Lr+yYx1q6PpJnZZoiSFS1lOItXESgO70pUgXB+33lNogXQLgafPyASrEO/fqHSa/mNolABGdwxGzBxhO72LBhPvVbDRRPqn1oGxqKcSnyczDjjLHchr+Nqy/QqhuFewLI=","encoding":{"content":["pkcs8","ecdsa"],"type":["scrypt","xsalsa20-poly1305"],"version":"3"},"address":"0x03f2fb46fbe96a8d4fda541abd1f5cd89daf0d07e1bba6f7ae36338a204afda609","meta":{"genesisHash":"0x91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3","isHardware":false,"name":"test","tags":[],"whenCreated":1754995148815}}"""
        )
    }
}