package jp.co.soramitsu.fearless_utils.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

inline fun <reified T> Gson.fromJson(src: String): T = fromJson(src, object : TypeToken<T>() {}.type)