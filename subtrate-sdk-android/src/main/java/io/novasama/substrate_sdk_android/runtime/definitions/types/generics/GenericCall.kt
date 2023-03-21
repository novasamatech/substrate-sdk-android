package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.errors.EncodeDecodeException
import io.novasama.substrate_sdk_android.runtime.metadata.callOrNull
import io.novasama.substrate_sdk_android.runtime.metadata.module.FunctionArgument
import io.novasama.substrate_sdk_android.runtime.metadata.module.MetadataFunction
import io.novasama.substrate_sdk_android.runtime.metadata.module.Module
import io.novasama.substrate_sdk_android.runtime.metadata.moduleOrNull
import io.novasama.substrate_sdk_android.scale.dataType.tuple
import io.novasama.substrate_sdk_android.scale.dataType.uint8

object GenericCall : Type<GenericCall.Instance>("GenericCall") {

    class Instance(
        val module: Module,
        val function: MetadataFunction,
        val arguments: Map<String, Any?>
    )

    private val indexCoder = tuple(uint8, uint8)

    override val isFullyResolved = true

    override fun decode(scaleCodecReader: ScaleCodecReader, runtime: RuntimeSnapshot): Instance {
        val (moduleIndex, callIndex) = indexCoder.read(scaleCodecReader)
            .run { first.toInt() to second.toInt() }

        val (module, function) = getModuleAndFunctionOrThrow(runtime, moduleIndex, callIndex)

        val arguments = function.arguments.associate { argumentDefinition ->
            argumentDefinition.name to argumentDefinition.typeOrThrow()
                .decode(scaleCodecReader, runtime)
        }

        return Instance(module, function, arguments)
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: Instance
    ) = with(value) {
        val callIndex = value.function.index.run { first.toUByte() to second.toUByte() }

        indexCoder.write(scaleCodecWriter, callIndex)

        function.arguments.forEach { argumentDefinition ->
            argumentDefinition.typeOrThrow()
                .encodeUnsafe(scaleCodecWriter, runtime, arguments[argumentDefinition.name])
        }
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is Instance
    }

    private fun FunctionArgument.typeOrThrow() =
        type ?: throw EncodeDecodeException("Argument $name is not resolved")

    private fun getModuleAndFunctionOrThrow(
        runtime: RuntimeSnapshot,
        moduleIndex: Int,
        callIndex: Int
    ): Pair<Module, MetadataFunction> {
        val module =
            runtime.metadata.moduleOrNull(moduleIndex) ?: callNotFound(moduleIndex, callIndex)
        val call = module.callOrNull(callIndex) ?: callNotFound(moduleIndex, callIndex)

        return module to call
    }

    private fun callNotFound(moduleIndex: Int, callIndex: Int): Nothing {
        throw EncodeDecodeException("No call found for index ($moduleIndex, $callIndex)")
    }
}
