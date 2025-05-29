package io.novasama.substrate_sdk_android.common

/**
 * Complexity: O(n * log(n))
 */
fun List<Long>.median(): Long = sorted().let {
    val middleRight = it[it.size / 2]
    val middleLeft = it[(it.size - 1) / 2] // will be same as middleRight if list size is odd

    (middleLeft + middleRight) / 2
}