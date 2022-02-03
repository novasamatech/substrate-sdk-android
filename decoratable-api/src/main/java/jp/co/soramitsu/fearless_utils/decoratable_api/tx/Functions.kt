package jp.co.soramitsu.fearless_utils.decoratable_api.tx

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericCall
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.MetadataFunction
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Module
import jp.co.soramitsu.fearless_utils.decoratable_api.SubstrateApi
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encodeToDynamicStructure
import kotlin.reflect.KType

class Function0(
    module: Module,
    function: MetadataFunction,
    api: SubstrateApi,
) : FunctionBase(
    module, function, api
) {
    operator fun invoke() = createExtrinsic(emptyMap())
}

class Function1<A1>(
    module: Module,
    function: MetadataFunction,
    api: SubstrateApi,
    private val a1Type: KType,
) : FunctionBase(
    module, function, api
) {
    operator fun invoke(argument: A1): SubmittableExtrinsic {
        require(function.arguments.size == 1)

        return createExtrinsic(
            mapOf(
                function.arguments.first().name to api.options.scale.encodeToDynamicStructure(a1Type, argument)
            )
        )
    }
}

class Function2<A1, A2>(
    module: Module,
    function: MetadataFunction,
    api: SubstrateApi,
    private val a1Type: KType,
    private val a2Type: KType
) : FunctionBase(
    module, function, api
) {
    operator fun invoke(arg1: A1, arg2: A2): SubmittableExtrinsic {
        require(function.arguments.size == 2)

        val scale = api.options.scale

        return createExtrinsic(
            mapOf(
                function.arguments[0].name to scale.encodeToDynamicStructure(a1Type, arg1),
                function.arguments[1].name to scale.encodeToDynamicStructure(a2Type, arg2),
            )
        )
    }
}

abstract class FunctionBase(
    private val module: Module,
    protected val function: MetadataFunction,
    protected val api: SubstrateApi,
) {

    protected fun createExtrinsic(vararg values: Any?): SubmittableExtrinsic {
        require(values.size == function.arguments.size)

        val argumentMap = function.arguments.zip(values) { argument, value -> argument.name to value }
            .toMap()

        return createExtrinsic(argumentMap)
    }

    protected fun createExtrinsic(argumentMap: Map<String, Any?>) = SubmittableExtrinsic(GenericCall.Instance(module, function, argumentMap), api)
}
