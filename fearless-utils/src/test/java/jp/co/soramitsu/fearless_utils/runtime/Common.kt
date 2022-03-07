package jp.co.soramitsu.fearless_utils.runtime

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.getFileContentFromResources
import jp.co.soramitsu.fearless_utils.gson_codec.GsonCodec

object RealRuntimeProvider {

    fun buildRuntime(networkName: String): RuntimeSnapshot {
        val metadataRaw = readRawMetadata(networkName)
        val jsons = listOf(readDefaultJson(), readNetworkJson(networkName))

        return RuntimeFactory(GsonCodec(Gson())).create(metadataRaw, jsons)
    }

    fun buildRuntimeV14(networkName: String): RuntimeSnapshot {
        val metadataRaw = getFileContentFromResources("${networkName}_metadata_v14")
        val jsons = listOf(readNetworkJson(networkName))

        return RuntimeFactory(GsonCodec(Gson())).create(metadataRaw, jsons)
    }

    fun readRawMetadata(networkName: String = "kusama") =
        getFileContentFromResources("${networkName}_metadata")

    private fun readDefaultJson() = getFileContentFromResources("default.json")
    private fun readNetworkJson(networkName: String) = getFileContentFromResources("${networkName}.json")
}
