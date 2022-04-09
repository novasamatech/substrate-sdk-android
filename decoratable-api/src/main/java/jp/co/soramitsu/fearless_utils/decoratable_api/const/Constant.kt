package jp.co.soramitsu.fearless_utils.decoratable_api.const

import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromByteArrayOrNull
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Constant as MetadataConstant
import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.AnyBinding
import jp.co.soramitsu.fearless_utils.decoratable_api.util.binding.BindingContext

class Constant<R>(
    private val runtime: RuntimeSnapshot,
    private val metadataConstant: MetadataConstant,
    private val bindingContext: BindingContext,
    private val binding: AnyBinding<R>
) {

    val value by lazy(LazyThreadSafetyMode.NONE) {
        createValue()
    }

    operator fun invoke(): R = value

    private fun createValue(): R {
        val decoded = metadataConstant.type?.fromByteArrayOrNull(runtime, metadataConstant.value)

        return binding(bindingContext, decoded)
    }
}
