package jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import jp.co.soramitsu.fearless_utils.extensions.fromSignedBytes
import jp.co.soramitsu.fearless_utils.extensions.pad
import jp.co.soramitsu.fearless_utils.extensions.toSignedBytes
import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.scale.utils.directWrite
import java.math.BigInteger
import java.nio.ByteOrder

val i8 = IntType(8)
val i16 = IntType(16)
val i32 = IntType(32)
val i64 = IntType(64)
val i128 = IntType(128)
val i256 = IntType(256)

class IntType(bits: Int) : NumberType("i$bits") {

    init {
        require(bits % 8 == 0)
    }

    val bytes = bits / 8

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: RuntimeSnapshot, value: BigInteger) {
        val bytes = value.toSignedBytes(
            resultByteOrder = ByteOrder.LITTLE_ENDIAN,
            expectedBytesSize = bytes
        )

        scaleCodecWriter.directWrite(bytes)
    }

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): BigInteger {
        val bytes = scaleCodecReader.readByteArray(bytes)

        return bytes.fromSignedBytes(originByteOrder = ByteOrder.LITTLE_ENDIAN)
    }
}