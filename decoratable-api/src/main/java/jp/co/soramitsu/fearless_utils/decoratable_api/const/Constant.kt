package jp.co.soramitsu.fearless_utils.decoratable_api.const

import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.RpcBinding
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Constant as MetadataConstant

class Constant<R>(
    val metadataConstant: MetadataConstant,
    val binding: ConstantsBinding<R>
)