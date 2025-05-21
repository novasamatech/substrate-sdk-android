package io.novasama.substrate_sdk_android.koltinx_serialization_scale.decoder

import kotlinx.serialization.modules.SerializersModule

class EnumDecoder(
    serializersModule: SerializersModule,
    value: Any?
) : PrimitiveDecoder(serializersModule, value)
