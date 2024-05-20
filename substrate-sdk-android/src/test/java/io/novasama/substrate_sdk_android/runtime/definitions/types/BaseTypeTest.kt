package io.novasama.substrate_sdk_android.runtime.definitions.types

import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.DynamicTypeResolver
import io.novasama.substrate_sdk_android.runtime.definitions.dynamic.extentsions.GenericsExtension
import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypeRegistry
import io.novasama.substrate_sdk_android.runtime.definitions.registry.v13Preset
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.BooleanType
import io.novasama.substrate_sdk_android.runtime.definitions.types.primitives.u8
import io.novasama.substrate_sdk_android.runtime.metadata.ExtrinsicMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.module.Event
import io.novasama.substrate_sdk_android.runtime.metadata.module.FunctionArgument
import io.novasama.substrate_sdk_android.runtime.metadata.module.MetadataFunction
import io.novasama.substrate_sdk_android.runtime.metadata.module.Module
import io.novasama.substrate_sdk_android.runtime.metadata.module.Storage
import java.math.BigInteger

abstract class BaseTypeTest {

    protected val typeRegistry = TypeRegistry(
        v13Preset(),
        dynamicTypeResolver = DynamicTypeResolver(
            extensions = DynamicTypeResolver.DEFAULT_COMPOUND_EXTENSIONS + listOf(GenericsExtension)
        )
    )

    protected val runtime: RuntimeSnapshot = RuntimeSnapshot(
        typeRegistry = typeRegistry,
        metadata = RuntimeMetadata(
            metadataVersion = 1,
            modules = mapOf(
                "A" to Module(
                    name = "A",
                    storage = Storage("_A", emptyMap()),
                    calls = mapOf(
                        "B" to MetadataFunction(
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
                                BooleanType,
                                u8
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