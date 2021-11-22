package jp.co.soramitsu.fearless_utils.decoratable_api.const

import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromByteArrayOrNull
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Constant as MetadataConstant

class Constant<R>(
    private val runtime: RuntimeSnapshot,
    private val metadataConstant: MetadataConstant,
    private val binding: ConstantsBinding<R>
) {

    val value = createValue()

    operator fun invoke(): R = value

    private fun createValue(): R {
        val decoded = metadataConstant.type?.fromByteArrayOrNull(runtime, metadataConstant.value)

        return binding(decoded)
    }
}
