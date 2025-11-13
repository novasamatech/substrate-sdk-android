package io.novasama.substrate_sdk_android.common

/**
 * Complexity: O(n * log(n))
 */
fun List<Long>.median(): Double = sorted().let {
    val middleRight = it[it.size / 2]
    val middleLeft = it[(it.size - 1) / 2]

    (middleLeft + middleRight) / 2.0
}