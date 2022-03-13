package io.github.nova_wallet.substrate_sdk_android.codegen

import com.android.build.api.variant.AndroidComponentsExtension
import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.gson_codec.GsonCodec
import kotlinx.coroutines.runBlocking
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import java.io.File

interface SubstrateCodegenExtension {
    val nodeUrl: Property<String>
}

class SubstrateCodegenPlugin : Plugin<Project> {

    @Suppress("UnstableApiUsage")
    override fun apply(project: Project) {
        val substrateExtension = project.extensions.create("substrate", SubstrateCodegenExtension::class.java)

        val codeDir = File("${project.buildDir}/generated/substrate")

        val androidExtensions = project.extensions.findByType(AndroidComponentsExtension::class.java)
        androidExtensions?.finalizeDsl {
            it.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).java.srcDirs(codeDir)
        }
        // TODO add sources for java projects
////        val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
//        project.convention.getPlugin(AndroidPl)
//        val main: SourceSet = javaConvention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)


//        main.java.srcDirs.add(codeDir)

        project.tasks.create("generateKotlinApi") {
            it.group = "substrate"
            it.doFirst {
//                Codegen().generate(codeDir)

                val gson = Gson()
                val gsonCodec = GsonCodec(gson)
                val runtimeMetadataRetriever = RuntimeMetadataRetriever(gsonCodec, substrateExtension.nodeUrl.get())

                runBlocking {
                    val runtime = runtimeMetadataRetriever.constructRuntime()

                    print(runtime.metadata.modules.values.joinToString(separator = "\n") { it.name })
                }
            }
        }
    }
}
