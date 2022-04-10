package io.github.nova_wallet.substrate_sdk_android.codegen.api.tx

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.nova_wallet.substrate_sdk_android.codegen.api.BaseModuleElementCodegen
import io.github.nova_wallet.substrate_sdk_android.codegen.common.TypeUnfolding
import io.github.nova_wallet.substrate_sdk_android.codegen.common.requireResolved
import io.github.nova_wallet.substrate_sdk_android.codegen.common.snakeToLowerCamelCase
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.MetadataFunction
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Module
import org.slf4j.Logger

private const val MAX_SUPPORTED_FUNCTION_ARITY = 3

class TxCodegen(
    private val logger: Logger,
    typeUnfolding: TypeUnfolding
) : BaseModuleElementCodegen(
    elementSubPackage = "tx",
    sectionInterfaceName = "DecoratableTx",
    parentInterfaceName = "DecoratableFunctions",
    fileName = "Tx",
    typeUnfolding = typeUnfolding
) {

    override fun FileSpec.Builder.generateChildInterfaceExtensions(module: Module, childInterfaceType: TypeName) {
        module.calls?.values?.forEach { function ->
            functionProperty(function, childInterfaceType)?.let {
                addProperty(it)

                addFunction(functionNamedArgsExtension(function, childInterfaceType))
            }
        }
    }

    private fun functionProperty(
        function: MetadataFunction,
        functionInterfaceType: TypeName,
    ): PropertySpec? {
        val functionReturnType = functionReturnType(function) ?: return null

        val getterBlock = """
            return decorator.%decoratorFunction:M(%entryName:S)
        """.trimIndent()

        val getterArgs = mapOf(
            "decoratorFunction" to functionDecoratorFunction(function),
            "entryName" to function.name,
        )

        return PropertySpec.builder(name = functionExtensionName(function), type = functionReturnType)
            .receiver(functionInterfaceType)
            .getter(
                FunSpec.getterBuilder()
                    .addCode(
                        CodeBlock.builder()
                            .addNamed(getterBlock, getterArgs)
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun functionNamedArgsExtension(
        function: MetadataFunction,
        functionInterfaceType: TypeName
    ): FunSpec {
        val functionExtensionName = functionExtensionName(function)

        val functionArgumentTypeNames = functionArgumentTypeNames(function)
        val functionArgumentNames = function.arguments.map { it.name.snakeToLowerCamelCase() }

        val functionParameterSpecs = functionArgumentTypeNames.zip(functionArgumentNames) { typeName, name ->
            ParameterSpec.builder(name, typeName)
                .build()
        }

        val delegateInvocationArguments = functionArgumentNames.joinToString(separator = ", ") { "%N" }

        return FunSpec.builder(functionExtensionName)
            .receiver(functionInterfaceType)
            .addParameters(functionParameterSpecs)
            .returns(submittableExtrinsicClassName())
            .addStatement(
                "return this@$functionExtensionName.$functionExtensionName.invoke($delegateInvocationArguments)",
                *functionArgumentNames.toTypedArray()
            )
            .build()
    }

    private fun functionExtensionName(function: MetadataFunction) = function.name.snakeToLowerCamelCase()

    private fun functionDecoratorFunction(function: MetadataFunction): MemberName {
        val functionName = "function${function.arguments.size}"

        return MemberName(elementApiPackage, functionName, isExtension = true)
    }

    private fun functionReturnType(function: MetadataFunction): TypeName? {
        val arity = function.arguments.size

        if (arity > MAX_SUPPORTED_FUNCTION_ARITY) {
            logger.warn("Skipped function ${function.name} since it has arity $arity which is greater then maximum supported ($MAX_SUPPORTED_FUNCTION_ARITY)")

            return null
        }

        val returnTypeRaw = ClassName(elementApiPackage, "Function$arity")

        if (arity == 0) return returnTypeRaw

        return returnTypeRaw.parameterizedBy(functionArgumentTypeNames(function))
    }

    private fun submittableExtrinsicClassName() = ClassName(elementApiPackage, "SubmittableExtrinsic")

    private fun functionArgumentTypeNames(function: MetadataFunction): List<TypeName> {
        return function.arguments.map { argument ->
            val argumentType = argument.type
            argumentType.requireResolved(parentLabel = function.name, childLabel = argument.name)

            argumentType.toTypeName(parentType = function.name)
        }
    }
}
