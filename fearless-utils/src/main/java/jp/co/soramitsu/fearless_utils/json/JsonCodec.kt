package jp.co.soramitsu.fearless_utils.json

interface JsonCodec {

    fun <T> fromJson(source: String, argumentClass: Class<T>): T

    fun <T> fromParsedHierarchy(hierarchy: Any?, argumentClass: Class<T>): T

    fun <T> toJson(value: T): String
}

inline fun <reified T> JsonCodec.fromParsedHierarchy(hierarchy: Any?): T {
    return fromParsedHierarchy(hierarchy, T::class.java)
}

inline fun <reified T> JsonCodec.fromJson(source: String): T {
    return fromJson(source, T::class.java)
}
