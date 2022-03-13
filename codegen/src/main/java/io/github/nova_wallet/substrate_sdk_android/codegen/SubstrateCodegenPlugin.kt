package io.github.nova_wallet.substrate_sdk_android.codegen

import org.gradle.api.Plugin
import org.gradle.api.Project

class SubstrateCodegenPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.create("SampleTask") {
            it.doFirst {
                Codegen().test()
            }
        }
    }
}
