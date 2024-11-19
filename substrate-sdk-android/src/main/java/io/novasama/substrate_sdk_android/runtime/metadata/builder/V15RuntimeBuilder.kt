package io.novasama.substrate_sdk_android.runtime.metadata.builder

import io.novasama.substrate_sdk_android.runtime.definitions.registry.TypeRegistry
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.RuntimeMetadataReader
import io.novasama.substrate_sdk_android.runtime.metadata.SignedExtensionMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.module.RuntimeApi
import io.novasama.substrate_sdk_android.runtime.metadata.module.RuntimeApiMethod
import io.novasama.substrate_sdk_android.runtime.metadata.module.RuntimeApiMethodParam
import io.novasama.substrate_sdk_android.runtime.metadata.v15.RuntimeApiMetadataV15
import io.novasama.substrate_sdk_android.runtime.metadata.v15.RuntimeApiMethodMetadata
import io.novasama.substrate_sdk_android.runtime.metadata.v15.RuntimeMetadataSchemaV15
import io.novasama.substrate_sdk_android.runtime.metadata.v15.apis
import io.novasama.substrate_sdk_android.runtime.metadata.v15.inputs
import io.novasama.substrate_sdk_android.runtime.metadata.v15.methods
import io.novasama.substrate_sdk_android.runtime.metadata.v15.name
import io.novasama.substrate_sdk_android.runtime.metadata.v15.outputType
import io.novasama.substrate_sdk_android.runtime.metadata.v15.type
import io.novasama.substrate_sdk_android.scale.EncodableStruct

object V15RuntimeBuilder : RuntimeBuilder {

    override fun buildMetadata(
        reader: RuntimeMetadataReader,
        typeRegistry: TypeRegistry,
        fallbackSignedExtensions: List<SignedExtensionMetadata>
    ): RuntimeMetadata {
        val v14 = V14RuntimeBuilder.buildMetadata(reader, typeRegistry, fallbackSignedExtensions)

        return RuntimeMetadata(
            metadataVersion = v14.metadataVersion,
            modules = v14.modules,
            extrinsic = v14.extrinsic,
            apis = constructRuntimeApis(reader.metadataV15, typeRegistry)
        )
    }

    private fun constructRuntimeApis(
        metadata: EncodableStruct<RuntimeMetadataSchemaV15>,
        typeRegistry: TypeRegistry
    ): List<RuntimeApi> {
        return metadata.apis.map { apiStruct ->
            constructRuntimeApi(apiStruct, typeRegistry)
        }
    }

    private fun constructRuntimeApi(
        apiStruct: EncodableStruct<RuntimeApiMetadataV15>,
        typeRegistry: TypeRegistry
    ): RuntimeApi {
        return RuntimeApi(
            name = apiStruct.name,
            methods = apiStruct.methods.map {
                constructRuntimeApiMethod(
                    parentName = apiStruct.name,
                    methodStruct = it,
                    typeRegistry = typeRegistry
                )
            }
        )
    }

    private fun constructRuntimeApiMethod(
        parentName: String,
        methodStruct: EncodableStruct<RuntimeApiMethodMetadata>,
        typeRegistry: TypeRegistry
    ): RuntimeApiMethod {
        return RuntimeApiMethod(
            name = methodStruct.name,
            inputs = methodStruct.inputs.map { paramStruct ->
                RuntimeApiMethodParam(
                    name = paramStruct.name,
                    type = typeRegistry[paramStruct.type]
                )
            },
            output = typeRegistry[methodStruct.outputType],
            apiName = parentName
        )
    }
}
