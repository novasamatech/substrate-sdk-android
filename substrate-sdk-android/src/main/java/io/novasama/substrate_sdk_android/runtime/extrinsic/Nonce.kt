package io.novasama.substrate_sdk_android.runtime.extrinsic

import java.math.BigInteger

/**
 * Structure for Nonce that is capable of carrying out information about multiple to-be-submitted txs
 */
data class Nonce(
    /**
     * Nonce that should be used to submit first tx
     */
    val baseNonce: BigInteger,
    /**
     * Offset from the [baseNonce] for the case when multiple txs are being submitted
     */
    val offset: BigInteger
) {

    val nonce: BigInteger = baseNonce + offset

    companion object {

        fun singleTx(nonce: BigInteger): Nonce {
            return Nonce(baseNonce = nonce, offset = BigInteger.ZERO)
        }

        fun zero(): Nonce = singleTx(BigInteger.ZERO)
    }
}

fun Nonce.replaceBaseNone(baseNonce: BigInteger): Nonce {
    return copy(baseNonce = baseNonce)
}
