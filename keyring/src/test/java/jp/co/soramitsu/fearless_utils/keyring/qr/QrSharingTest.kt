package jp.co.soramitsu.fearless_utils.keyring.qr

import jp.co.soramitsu.fearless_utils.any
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class QrSharingTest {

    @Mock
    lateinit var succeedingFormat: QrFormat

    @Mock
    lateinit var failingFormat: QrFormat

    lateinit var qrSharing: QrSharing

    @Before
    fun setup() {
        given(failingFormat.decode(any())).will { throw QrFormat.InvalidFormatException("error") }
        given(succeedingFormat.decode(any())).willReturn(QrFormat.Payload("test"))

        qrSharing = QrSharing(
            decodingFormats = listOf(failingFormat, succeedingFormat),
            encodingFormat = succeedingFormat
        )
    }

    @Test
    fun `should decode using non failing format`() {
        val result = qrSharing.decode("test")

        assertEquals("test", result.address)
    }
}
