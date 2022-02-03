package jp.co.soramitsu.fearless_utils.decoratable_api.tx

operator fun <T> Function1<List<T>>.invoke(
    vararg arguments: T
) = invoke(arguments.toList())
