package io.github.nova_wallet.substrate_sdk_android.codegen

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class SubstrateCodegenPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.create("SampleTask") {
            it.doFirst {
                val codeDir = File("${project.buildDir}/generated/substrate")

                Codegen().test(codeDir)
            }
        }
    }
}
