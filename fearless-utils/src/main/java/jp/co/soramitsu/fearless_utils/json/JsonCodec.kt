package jp.co.soramitsu.fearless_utils.json

interface JsonCodec {

    fun <T> fromJson(source: String, argumentClass: Class<T>): T

    fun <T> toJson(value: T): String
}