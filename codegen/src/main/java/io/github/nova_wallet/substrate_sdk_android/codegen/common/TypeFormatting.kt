package io.github.nova_wallet.substrate_sdk_android.codegen.common

object TypeFormatting {

    fun unknownType(parent: String, child: String): Nothing {
        throw IllegalArgumentException("Unknown type in $parent::$child")
    }

    infix fun String.joinTypeName(child: String) = "$this.$child"
}
