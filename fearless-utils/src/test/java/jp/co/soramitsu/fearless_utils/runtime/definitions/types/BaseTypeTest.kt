package jp.co.soramitsu.fearless_utils.runtime.definitions.types

import jp.co.soramitsu.fearless_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.DynamicTypeResolver
import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.extentsions.GenericsExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.substratePreParsePreset
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.BooleanType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u8
import jp.co.soramitsu.fearless_utils.runtime.metadata.Event
import jp.co.soramitsu.fearless_utils.runtime.metadata.ExtrinsicMetadata
import jp.co.soramitsu.fearless_utils.runtime.metadata.Function
import jp.co.soramitsu.fearless_utils.runtime.metadata.FunctionArgument
import jp.co.soramitsu.fearless_utils.runtime.metadata.MetadataType
import jp.co.soramitsu.fearless_utils.runtime.metadata.Module
import jp.co.soramitsu.fearless_utils.runtime.metadata.RuntimeMetadata
import jp.co.soramitsu.fearless_utils.runtime.metadata.Storage
import java.math.BigInteger

abstract class BaseTypeTest {

    protected val typeRegistry = TypeRegistry(
        substratePreParsePreset(),
        dynamicTypeResolver = DynamicTypeResolver(
            extensions = DynamicTypeResolver.DEFAULT_COMPOUND_EXTENSIONS + listOf(GenericsExtension)
        )
    )

    protected val runtime: RuntimeSnapshot = RuntimeSnapshot(
        typeRegistry = typeRegistry,
        metadata = RuntimeMetadata(
            runtimeVersion = BigInteger.ONE,
            modules = mapOf(
                "A" to Module(
                    name = "A",
                    storage = Storage("_A", emptyMap()),
                    calls = mapOf(
                        "B" to Function(
                            name = "B",
                            arguments = listOf(
                                FunctionArgument(
                                    name = "arg1",
                                    type = BooleanType
                                ),
                                FunctionArgument(
                                    name = "arg2",
                                    type = u8
                                )
                            ),
                            documentation = emptyList(),
                            index = 1 to 0
                        )
                    ),
                    events = mapOf(
                        "A" to Event(
                            name = "A",
                            arguments = listOf(
                                MetadataType(
                                    definition="bool",
                                    derivedType = BooleanType,
                                ),
                                MetadataType(
                                    definition="u8",
                                    derivedType = u8,
                                )
                            ),
                            documentation = emptyList(),
                            index = 1 to 0
                        ),
                    ),
                    constants = emptyMap(),
                    errors = emptyMap(),
                    index = BigInteger.ONE
                )
            ),
            extrinsic = ExtrinsicMetadata(
                version = BigInteger.ONE,
                signedExtensions = emptyList()
            )
        )
    )

}