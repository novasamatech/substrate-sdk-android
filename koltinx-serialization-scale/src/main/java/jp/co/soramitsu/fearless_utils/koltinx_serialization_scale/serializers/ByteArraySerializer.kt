package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.serializers

import kotlinx.serialization.serializer

val byteArraySerializer = serializer<ByteArray>()
val bitFlagsSerializer = serializer<Set<String>>()
