package io.novasama.substrate_sdk_android.encrypt.json

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import org.junit.Test

class Ed25519JsonEncoderTest : BaseSubstrateJsonEncoderTest() {

    override val encryptionType = EncryptionType.ED25519

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
            json = """{"encoded":"2WEZBQOw8o8/Fw5mzM37cHQ9NqdtJNm2FmTyx0tLsSIAAAIAAQAAAAgAAADIGDb9IIJKKPBxDYr0a/3SIJPEOFR/u12rsWJqrdugf5K/GmhwTredg6aaJ3Uq2d59Llxy4VjvDGbzTHEYBefkqb4MxLB79i7Hp0g9w85cAN2R4aL90jcCEl/E8+NGQZCKfpyjlK/bRpKrHDqLA2VXFBvcGo5afdzk4LPc9xbZQ61iLIPFKrVJ+YoHwkqOZqNq+ANNagH+3Rvux8zl","encoding":{"content":["pkcs8","ed25519"],"type":["scrypt","xsalsa20-poly1305"],"version":"3"},"address":"14qPJnGe54DjZLpXwWjNSH3WNE3tqu8qzXypV1c9whwBmknM","meta":{"genesisHash":"0x91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3","isHardware":false,"name":"test","tags":[],"whenCreated":1754992507089}}"""
        )
    }

    @Test
    fun `decode should be compatible with external sources with derivation path`() {
        testExternalCompatibility(
            derivationPath = "//1//2",
            json = """{"encoded":"CusNamn8pjH+FwJxCApPL68XJwI1cgUyQeh5NfJl7gUAAAIAAQAAAAgAAAANg+bQtV0pmOQm2eG809RqO2UNQ18Jq55UxgS+rwdtBjz+yxnegLz5J1d1G81eTCXlyK4SM1ldVNwzdgqPLHO83YR0MrSxblZv/ZcleyJTfL+O9wMoJE0eaVHoViTy77pbQICQEJ55bFLNbm4iIEmDQyeT1lzIyFDBtHHS4ETXyOn3HyJB5MVQ6t8rxQl/q0PsSBzHUavnrCoVtnfw","encoding":{"content":["pkcs8","ed25519"],"type":["scrypt","xsalsa20-poly1305"],"version":"3"},"address":"1cQcHKxEGyAw8SqurBhGWmkeFN5C8CK1Zu4pQ6wcSxAgWJ6","meta":{"genesisHash":"0x91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3","isHardware":false,"name":"test","tags":[],"whenCreated":1754994906148}}"""
        )
    }
}