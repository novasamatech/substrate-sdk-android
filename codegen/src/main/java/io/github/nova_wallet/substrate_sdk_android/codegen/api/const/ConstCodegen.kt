package io.github.nova_wallet.substrate_sdk_android.codegen.api.const

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.nova_wallet.substrate_sdk_android.codegen.api.BaseModuleElementCodegen
import io.github.nova_wallet.substrate_sdk_android.codegen.common.TypeUnfolding
import io.github.nova_wallet.substrate_sdk_android.codegen.common.asNullable
import io.github.nova_wallet.substrate_sdk_android.codegen.common.requireResolved
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Constant
import jp.co.soramitsu.fearless_utils.runtime.metadata.module.Module

class ConstCodegen(
    typeUnfolding: TypeUnfolding
) : BaseModuleElementCodegen(
    elementSubPackage = "const",
    sectionInterfaceName = "DecoratableConst",
    parentInterfaceName = "DecoratableConstantsModule",
    fileName = "Const",
    typeUnfolding = typeUnfolding
) {
    override fun FileSpec.Builder.generateChildInterfaceExtensions(module: Module, childInterfaceType: TypeName) {
        module.constants.values.forEach { constant ->
            val constantPropertyNullable = constantPropertyNullable(constant, childInterfaceType)
            addProperty(constantPropertyNullable)

            val constantPropertyNonNull = constantPropertyNonNull(constant, childInterfaceType)
            addProperty(constantPropertyNonNull)
        }
    }

    /*
    val SystemConst.blockHashCountOrNull: Constant<BigInteger>?
        get() = decorator.constant("BlockHashCount")
   */
    private fun constantPropertyNullable(
        constant: Constant,
        constantInterfaceType: TypeName,
    ): PropertySpec {

        val constantDecoratorFunction = constantDecoratorFunction(constant)

        return PropertySpec.builder(
            name = constantName(constant, nullable = true),
            type = constantReturnType(constant).asNullable()
        )
            .receiver(constantInterfaceType)
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return decorator.%M(%S)", constantDecoratorFunction, constant.name)
                    .build()
            )
            .build()
    }

    /*
    val TimestampConst.minimumPeriod
        get() = minimumPeriodOrNull ?: SubstrateApiException.constantNotFound("MinimumPeriod")
   */
    private fun constantPropertyNonNull(
        constant: Constant,
        constantInterfaceType: TypeName,
    ): PropertySpec {
        val nullableName = constantName(constant, nullable = true)

        val getterBlock = """
            return $nullableName ?: %substrateExceptionClass:T.%constantNotFound:M(%constantName:S)
            """.trimIndent()

        val getterArgs = mapOf(
            "substrateExceptionClass" to substrateExceptionClass(),
            "constantName" to constant.name,
            "constantNotFound" to substrateExceptionMember("constantNotFound"),
        )

        return PropertySpec.builder(
            name = constantName(constant, nullable = false),
            type = constantReturnType(constant)
        )
            .receiver(constantInterfaceType)
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

    private fun constantDecoratorFunction(constant: Constant): MemberName {
        return MemberName(elementApiPackage, "constant", isExtension = true)
    }

    private fun constantName(constant: Constant, nullable: Boolean) = elementName(constant.name.decapitalize(), nullable)

    private fun constantReturnType(constant: Constant): TypeName {
        val constantType = ClassName(elementApiPackage, "Constant")

        val type = constant.type
        type.requireResolved(parentLabel = constant.name, childLabel = "value")
        val typeName = type.toTypeName(parentType = constant.name)

        return constantType.parameterizedBy(typeName)
    }
}
