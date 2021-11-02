package jp.co.soramitsu.fearless_utils.runtime

import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.metadata.RuntimeMetadata

class RuntimeSnapshot(
    val typeRegistry: TypeRegistry,
    val metadata: RuntimeMetadata
)
