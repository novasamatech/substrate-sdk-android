package jp.co.soramitsu.fearless_utils.gson_codec

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.json.JsonCodec

class GsonCodec(val gson: Gson) : JsonCodec {

    override fun <T> fromJson(source: String, argumentClass: Class<T>): T {
        return gson.fromJson(source, argumentClass)
    }

    override fun <T> fromParsedHierarchy(hierarchy: Any?, argumentClass: Class<T>): T {
        val tree = gson.toJsonTree(hierarchy)

        return gson.fromJson(tree, argumentClass)
    }

    override fun <T> toJson(value: T): String {
        return gson.toJson(value)
    }
}
