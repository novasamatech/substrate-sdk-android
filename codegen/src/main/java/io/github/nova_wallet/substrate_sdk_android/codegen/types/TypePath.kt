package io.github.nova_wallet.substrate_sdk_android.codegen.types

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

class TypePath(
    val packageName: String,
    val typeName: String
) {

    companion object
}

val TypePath.isIdLike
    get() = typeName.toIntOrNull() != null

fun TypePath.toTypeName(): TypeName = ClassName(packageName, typeName)

fun TypePath.Companion.fromName(typeName: String): TypePath {
    val nameParts = typeName.split(".")

    return if (nameParts.size == 1) {
        TypePath(
            packageName = "",
            typeName = nameParts.first()
        )
    } else {
        TypePath(
            packageName = nameParts.dropLast(1).joinToString(separator = "."),
            typeName = nameParts.last()
        )
    }
}
