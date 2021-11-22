package jp.co.soramitsu.fearless_utils.decoratable_api.const

interface DecoratableConst {

    fun <R : DecoratableConstantsModule> decorate(
        moduleName: String,
        creator: DecoratableConstantsModule.() -> R
    ): R?
}