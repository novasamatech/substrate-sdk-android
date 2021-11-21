package jp.co.soramitsu.fearless_utils.runtime.metadata.builder

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.metadata.RuntimeMetadata
import jp.co.soramitsu.fearless_utils.scale.EncodableStruct
import jp.co.soramitsu.fearless_utils.scale.Schema

interface RuntimeMetadataBuilder<S : Schema<S>> {

    fun buildMetadata(
        metadataStruct: EncodableStruct<S>,
        metadataVersion: Int,
        typeRegistry: TypeRegistry
    ): RuntimeMetadata
}