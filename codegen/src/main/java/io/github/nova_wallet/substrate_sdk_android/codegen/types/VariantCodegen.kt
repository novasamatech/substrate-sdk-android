package io.github.nova_wallet.substrate_sdk_android.codegen.types

import com.squareup.kotlinpoet.*
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.Null
import java.io.File

class VariantCodegen(
    parentDirectory: File,
    configuration: Configuration,
    private val structTypeCodegen: StructTypeCodegen,
) : TypeCodegen<DictEnum>(parentDirectory, configuration) {

    override fun FileSpec.Builder.applyType(type: DictEnum, path: TypePath) {
        val rootClassBuilder = TypeSpec.classBuilder(path.typeName)
            .addModifiers(KModifier.SEALED)

        val rootClassName = ClassName(path.packageName, path.typeName)

        type.elements.values.forEach { entryValue ->
            val variantName = entryValue.name
            val variantType = entryValue.value.value ?: unknownType(type.name, variantName)

            val variantFieldTypeName = variantType.toTypeName(parentType = type.name)

            val childTypeSpec = when (variantType) {
                // object VariantName: Root()
                is Null -> {
                    TypeSpec.objectBuilder(variantName)
                        .superclass(rootClassName)
                        .build()
                }
                // VariantName(field1: TYPE1, field2: TYPE2,...): Root()
                is Struct -> {
                    val classBuilder = TypeSpec.classBuilder(variantName)
                        .superclass(rootClassName)

                    with(structTypeCodegen) {
                        classBuilder.applyStruct(variantType)
                    }

                    classBuilder.build()
                }
                // VariantName(variantName: TYPE): Root()
                else -> {
                    val variantFieldName = variantName.toLowerCase()

                    TypeSpec.classBuilder(variantName)
                        .primaryConstructor(
                            FunSpec.constructorBuilder()
                                .addParameter(variantFieldName, variantFieldTypeName)
                                .build()
                        )
                        .addProperty(
                            PropertySpec.builder(variantFieldName, variantFieldTypeName)
                                .initializer(variantFieldName)
                                .build()
                        )
                        .superclass(rootClassName)
                        .build()
                }
            }

            rootClassBuilder.addType(childTypeSpec)
        }

        addType(rootClassBuilder.build())
    }
}
