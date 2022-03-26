package jp.co.soramitsu.fearless_utils.koltinx_serialization_scale.types

import kotlinx.serialization.Serializable

@Serializable
object Tuple0

@Serializable
data class Tuple2<T1, T2>(val t1: T1, val t2: T2)

@Serializable
data class Tuple3<T1, T2, T3>(val t1: T1, val t2: T2, val t3: T3)

@Serializable
data class Tuple4<T1, T2, T3, T4>(val t1: T1, val t2: T2, val t3: T3, val t4: T4)

@Serializable
data class Tuple5<T1, T2, T3, T4, T5>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5)

@Serializable
data class Tuple6<T1, T2, T3, T4, T5, T6>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5, val t6: T6)

@Serializable
data class Tuple7<T1, T2, T3, T4, T5, T6, T7>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5, val t6: T6, val t7: T7)

@Serializable
data class Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5, val t6: T6, val t7: T7, val t8: T8)
