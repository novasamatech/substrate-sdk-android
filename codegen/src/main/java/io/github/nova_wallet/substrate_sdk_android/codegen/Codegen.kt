package io.github.nova_wallet.substrate_sdk_android.codegen

import com.squareup.kotlinpoet.*

class Codegen {

    fun generate(outputDir: java.io.File) {
        val greeterClass = ClassName("io.github.nova_wallet.substrate_sdk_android.codegen", "Greeter")
        val file = FileSpec.builder("io.github.nova_wallet.substrate_sdk_android.codegen", "HelloWorld")
            .addType(
                TypeSpec.classBuilder("Greeter")
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter("name", String::class)
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("name", String::class)
                            .initializer("name")
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("greet")
                            .addStatement("println(%P)", "Hello, \$name")
                            .build()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("main")
                    .addParameter("args", String::class, KModifier.VARARG)
                    .addStatement("%T(args[0]).greet()", greeterClass)
                    .build()
            )
            .build()

        file.writeTo(outputDir)
    }
}
