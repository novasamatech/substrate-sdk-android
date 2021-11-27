package jp.co.soramitsu.fearless_utils.decoratable_api.options

import jp.co.soramitsu.fearless_utils.decoratable_api.tx.SubmittableExtrinsic
import jp.co.soramitsu.fearless_utils.decoratable_api.util.scale.serializers.AsIsSerializer
import jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.Scale
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.generics.GenericCall
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

// FIXME once https://github.com/Kotlin/kotlinx.serialization/pull/1686 is in release
val apiExtraScaleModules = SerializersModule {
    polymorphic(GenericCall.Instance::class) {
        subclass(SubmittableExtrinsic::class, AsIsSerializer(SubmittableExtrinsic::class))
        subclass(GenericCall.Instance.Default::class, AsIsSerializer(GenericCall.Instance.Default::class))
    }
}

fun ApiScale() = Scale(apiExtraScaleModules)
