package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.hash.isPositive
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.Primitive
import io.novasama.substrate_sdk_android.scale.dataType.compactInt
import io.novasama.substrate_sdk_android.scale.dataType.uint
import java.math.BigInteger
import kotlin.math.ceil

typealias Bits = BooleanArray

object BitVec : Primitive<Bits>("BitVec") {

    private val TWO = BigInteger("2")

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): Bits {
        val sizeInBits = compactInt.read(scaleCodecReader).toInt()
        val sizeInBytes = sizeInBytes(sizeInBits)

        val bits = BooleanArray(sizeInBits)

        if (sizeInBits == 0) return booleanArrayOf()

        val bitsHolder = uint(sizeInBytes).read(scaleCodecReader)

        repeat(sizeInBits) { i ->
            val mask = TWO.pow(i)

            bits[i] = bitsHolder.and(mask).isPositive()
        }

        return bits
    }

    override fun encode(scaleCodecWriter: ScaleCodecWriter, runtime: RuntimeSnapshot, value: Bits) {
        val intValue = value.foldIndexed(BigInteger.ZERO) { index, acc, bit ->
            if (bit) {
                acc + TWO.pow(index)
            } else {
                acc
            }
        }

        val sizeInBytes = sizeInBytes(value.size)

        compactInt.write(scaleCodecWriter, value.size.toBigInteger())

        if (value.isNotEmpty()) {
            uint(sizeInBytes).write(scaleCodecWriter, intValue)
        }
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is List<*> && instance.all { it is Boolean }
    }

    private fun sizeInBytes(inBits: Int) = ceil(inBits / 8.0).toInt()
}
