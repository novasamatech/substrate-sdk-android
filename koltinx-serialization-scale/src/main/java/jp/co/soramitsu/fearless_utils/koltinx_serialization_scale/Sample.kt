package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale

import kotlinx.serialization.Serializable

@Serializable
class Sample(
    val a: Int,
    val b: String
)

fun test() {
    val sample = Sample(1, "242")

    print(Scale.encodeToDynamicStructure(sample))
}
