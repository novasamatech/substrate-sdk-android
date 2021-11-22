package jp.co.soramitsu.fearless_utils.decoratable_api

import java.lang.Exception

class SubstrateApiException(override val message: String): Exception(message) {

    companion object
}

fun SubstrateApiException.Companion.moduleNotFound(moduleName: String): Nothing {
    throw SubstrateApiException("Module $moduleName not found")
}