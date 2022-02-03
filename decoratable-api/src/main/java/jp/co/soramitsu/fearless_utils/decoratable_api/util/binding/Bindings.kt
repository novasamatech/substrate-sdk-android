package jp.co.soramitsu.fearless_utils.decoratable_api.util.binding

import jp.co.soramitsu.fearless_utils.json.JsonCodec
import jp.co.soramitsu.fearless_utils.json.fromParsedHierarchy
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.Scale
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.decodeFromDynamicStructure

interface BindingContext {

    val scale: Scale

    val jsonCodec: JsonCodec
}

class SimpleBindingContext(override val scale: Scale, override val jsonCodec: JsonCodec) : BindingContext

typealias Binding<I, O> = BindingContext.(I) -> O
typealias AnyBinding<O> = Binding<Any?, O>

object Bindings {

    @Suppress("unused")
    inline fun <reified T> dynamicBinder(): AnyBinding<T> = {
        scale.decodeFromDynamicStructure(it)
    }

    @Suppress("unused")
    inline fun <reified T> asJson(): AnyBinding<T> = {
        jsonCodec.fromParsedHierarchy(it)
    }
}
