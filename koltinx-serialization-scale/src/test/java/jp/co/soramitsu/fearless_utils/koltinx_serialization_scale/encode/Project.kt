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

@Serializable
object SingletonObject: Project() {
    override val name: String
        get() = "123"

}

fun main() {
    val data: Project = SingletonObject

    val json = Json.encodeToString(data)

    val decoded = Json.decodeFromString<Project>(json)

    print(decoded)
}
