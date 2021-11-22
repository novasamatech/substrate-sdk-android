package jp.co.soramitsu.fearless_utils.decoratable_api.util.ext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

internal inline fun <T> CoroutineScope.lazyAsync(crossinline producer: suspend () -> T) = lazy {
    async { producer() }
}
