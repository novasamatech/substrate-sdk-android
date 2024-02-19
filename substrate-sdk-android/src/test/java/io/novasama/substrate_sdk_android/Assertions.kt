package io.novasama.substrate_sdk_android

fun <T> assertListEquals(
    expected: List<T>,
    actual: List<T>,
    comparator: (T, T) -> Boolean = { expectedElement, actualElement ->
        expectedElement == actualElement
    }
) {
    expected.zip(actual).forEachIndexed { index, (expectedElement, actualElement) ->
        assert(comparator(expectedElement, actualElement)) {
            "$expectedElement != $actualElement at position $index"
        }
    }
}