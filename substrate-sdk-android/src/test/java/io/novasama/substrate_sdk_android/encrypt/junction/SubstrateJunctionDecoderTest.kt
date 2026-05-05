package io.novasama.substrate_sdk_android.encrypt.junction

import io.novasama.substrate_sdk_android.extensions.fromHex
import org.junit.Test

class SubstrateJunctionDecoderTest : JunctionTest() {

    override val decoder: JunctionDecoder = SubstrateJunctionDecoder

    @Test
    fun `single soft`() = performTest(
        path = "/1",
        expectedPassword = null,
        Junction(
            JunctionType.SOFT,
            "0100000000000000000000000000000000000000000000000000000000000000".fromHex()
        )
    )

    @Test
    fun `single hard`() = performTest(
        path = "//2",
        expectedPassword = null,
        Junction(
            JunctionType.HARD,
            "0200000000000000000000000000000000000000000000000000000000000000".fromHex()
        )
    )

    @Test
    fun `soft and hard`() = performTest(
        path = "//2/3",
        expectedPassword = null,
        Junction(
            JunctionType.HARD,
            "0200000000000000000000000000000000000000000000000000000000000000".fromHex()
        ),
        Junction(
            JunctionType.SOFT,
            "0300000000000000000000000000000000000000000000000000000000000000".fromHex()
        )
    )

    // numeric junction is serialized as 8-byte little-endian Long, then padded to 32 bytes
    @Test
    fun `numeric zero is padded`() = performTest(
        path = "/0",
        expectedPassword = null,
        Junction(
            JunctionType.SOFT,
            "0000000000000000000000000000000000000000000000000000000000000000".fromHex()
        )
    )

    @Test
    fun `numeric byte value is padded`() = performTest(
        path = "/255",
        expectedPassword = null,
        Junction(
            JunctionType.SOFT,
            "ff00000000000000000000000000000000000000000000000000000000000000".fromHex()
        )
    )

    @Test
    fun `numeric negative value is padded`() = performTest(
        path = "/-1",
        expectedPassword = null,
        Junction(
            JunctionType.SOFT,
            "ffffffffffffffff000000000000000000000000000000000000000000000000".fromHex()
        )
    )

    // hex shorter than 32 bytes is padded
    @Test
    fun `hex single byte is padded`() = performTest(
        path = "/0x01",
        expectedPassword = null,
        Junction(
            JunctionType.SOFT,
            "0100000000000000000000000000000000000000000000000000000000000000".fromHex()
        )
    )

    @Test
    fun `hex four bytes is padded`() = performTest(
        path = "/0xdeadbeef",
        expectedPassword = null,
        Junction(
            JunctionType.SOFT,
            "deadbeef00000000000000000000000000000000000000000000000000000000".fromHex()
        )
    )

    // hex of exactly 32 bytes is used as-is
    @Test
    fun `hex exactly 32 bytes is used as-is`() = performTest(
        path = "/0x0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20",
        expectedPassword = null,
        Junction(
            JunctionType.SOFT,
            "0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20".fromHex()
        )
    )

    // hex longer than 32 bytes is hashed via blake2b256
    @Test
    fun `hex longer than 32 bytes is blake2b256 hashed`() = performTest(
        path = "/0x0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f2021",
        expectedPassword = null,
        Junction(
            JunctionType.SOFT,
            "fcd2d9ace87052811d9f3427b58ff398d2e9ed83f301bc7ec1be8b593962f17d".fromHex()
        )
    )

    // non-hex string is SCALE-encoded; SCALE-encoded length < 32 → padded
    @Test
    fun `short string is scale encoded and padded`() = performTest(
        path = "/alice",
        expectedPassword = null,
        Junction(
            JunctionType.SOFT,
            "14616c6963650000000000000000000000000000000000000000000000000000".fromHex()
        )
    )

    // 31 UTF-8 bytes + 1 SCALE compact prefix = exactly 32 bytes → as-is
    @Test
    fun `string with scale length exactly 32 bytes is used as-is`() = performTest(
        path = "/" + "!".repeat(31),
        expectedPassword = null,
        Junction(
            JunctionType.SOFT,
            "7c21212121212121212121212121212121212121212121212121212121212121".fromHex()
        )
    )

    // 32 UTF-8 bytes + 1 SCALE compact prefix = 33 bytes → blake2b256
    @Test
    fun `string with scale length over 32 bytes is blake2b256 hashed`() = performTest(
        path = "/" + "!".repeat(32),
        expectedPassword = null,
        Junction(
            JunctionType.SOFT,
            "d07e935c632f1d79464c65126b521488217f0a6c89ad7eb62defff25aa657e34".fromHex()
        )
    )
}
