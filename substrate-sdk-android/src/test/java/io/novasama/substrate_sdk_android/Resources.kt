package io.novasama.substrate_sdk_android

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader

fun Any.getFileContentFromResources(fileName: String): String {
    return getResourceReader(fileName).readText()
}

fun Any.getResourceReader(fileName: String): Reader {
    val stream = javaClass.classLoader!!.getResourceAsStream(fileName)

    return BufferedReader(InputStreamReader(stream))
}