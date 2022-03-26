package io.github.nova_wallet.substrate_sdk_android.codegen.ext

import com.squareup.kotlinpoet.TypeName

fun TypeName.asNullable() = copy(nullable = true)
