package io.novasama.substrate_sdk_android.encrypt.qr

import io.novasama.substrate_sdk_android.common.assertThrows
import io.novasama.substrate_sdk_android.encrypt.qr.QrFormat.Payload
import io.novasama.substrate_sdk_android.encrypt.qr.formats.SubstrateQrFormat
import io.novasama.substrate_sdk_android.extensions.fromHex
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SubstrateQrFormatTest {
    private val address = "FiLhWLARS32oxm4s64gmEMSppAdugsvaAx1pCjweTLGn5Rf"
    private val publicKeyEncoded =
        "0x8ad2a3fba73321961cd5d1b8272aa95a21e75dd5b098fb36ed996961ac7b2931"
    private val name = "Russel"

    private val publicKeyBytes = publicKeyEncoded.fromHex()

    private val qrContentWithName =
        "substrate:FiLhWLARS32oxm4s64gmEMSppAdugsvaAx1pCjweTLGn5Rf:0x8ad2a3fba73321961cd5d1b8272aa95a21e75dd5b098fb36ed996961ac7b2931:Russel"
    private val qrContentWithoutName =
        "substrate:FiLhWLARS32oxm4s64gmEMSppAdugsvaAx1pCjweTLGn5Rf:0x8ad2a3fba73321961cd5d1b8272aa95a21e75dd5b098fb36ed996961ac7b2931"

    private val format = SubstrateQrFormat()

    @Test
    fun `should encode with name`() {
        val payload = Payload(address, publicKeyBytes, name)

        val result = format.encode(payload)

        assertEquals(qrContentWithName, result)
    }

    @Test
    fun `should encode without name`() {
        val payload = Payload(address, publicKeyBytes, null)

        val result = format.encode(payload)

        assertEquals(qrContentWithoutName, result)
    }

    @Test
    fun `should decode with name`() {
        val result = format.decode(qrContentWithName)

        assertEquals(name, result.name)
        assertEquals(address, result.address)
        assert(publicKeyBytes.contentEquals(result.publicKey))
    }

    @Test
    fun `should decode without name`() {
        val result = format.decode(qrContentWithoutName)

        assertEquals(null, result.name)
        assertEquals(address, result.address)
        assert(publicKeyBytes.contentEquals(result.publicKey))
    }

    @Test
    fun `should throw for wrong format`() {
        val wrongContent = "wrong"

        assertThrows<QrFormat.InvalidFormatException> {
            format.decode(wrongContent)
        }
    }

    @Test
    fun `should throw for not enough args format`() {
        val wrongContent = "substrate:123"

        assertThrows<QrFormat.InvalidFormatException> {
            format.decode(wrongContent)
        }
    }

    @Test
    fun `should throw for too much args format`() {
        val wrongContent = "$qrContentWithName:123"

        assertThrows<QrFormat.InvalidFormatException> {
            format.decode(wrongContent)
        }
    }
}