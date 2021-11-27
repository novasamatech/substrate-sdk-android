// This file was automatically generated from polymorphism.md by Knit tool. Do not edit.
package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.encode

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
sealed class Project {
    abstract val name: String
}
            
@Serializable
class OwnedProject(override val name: String, val owner: String) : Project()

fun main() {
    val data: Project = OwnedProject("kotlinx.coroutines", "kotlin")
    println(Json.encodeToJsonElement(data)) // Serializing data of compile-time type Project
}
