package io.novasama.substrate_sdk_android.encrypt.json

import com.google.gson.Gson
import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import io.novasama.substrate_sdk_android.encrypt.MultiChainEncryption
import io.novasama.substrate_sdk_android.encrypt.json.JsonSeedDecodingException.IncorrectPasswordException
import io.novasama.substrate_sdk_android.encrypt.json.JsonSeedDecodingException.InvalidJsonException
import io.novasama.substrate_sdk_android.encrypt.json.coders.content.ContentCoderFactory
import io.novasama.substrate_sdk_android.encrypt.json.coders.content.decode
import io.novasama.substrate_sdk_android.encrypt.json.coders.type.TypeCoderFactory
import io.novasama.substrate_sdk_android.encrypt.json.coders.type.decode
import io.novasama.substrate_sdk_android.encrypt.model.ImportAccountData
import io.novasama.substrate_sdk_android.encrypt.model.ImportAccountMeta
import io.novasama.substrate_sdk_android.encrypt.model.JsonAccountData
import io.novasama.substrate_sdk_android.encrypt.model.NetworkTypeIdentifier
import io.novasama.substrate_sdk_android.ss58.SS58Encoder.addressByteOrNull
import org.bouncycastle.util.encoders.Base64

sealed class JsonSeedDecodingException : Exception() {
    class InvalidJsonException : JsonSeedDecodingException()
    class IncorrectPasswordException : JsonSeedDecodingException()
    class UnsupportedEncryptionTypeException : JsonSeedDecodingException()
}

private fun MultiChainEncryption.Companion.from(name: String): MultiChainEncryption {
    return when (name) {
        "ethereum" -> MultiChainEncryption.Ethereum
        else -> {
            MultiChainEncryption.Substrate(EncryptionType.fromString(name))
        }
    }
}

class JsonSeedDecoder(private val gson: Gson) {

    fun extractImportMetaData(json: String): ImportAccountMeta {
        val jsonData = decodeJson(json)

        try {
            val address = jsonData.address
            val networkType = getNetworkTypeIdentifier(address, jsonData.meta.genesisHash)

            val encryptionTypeRaw = jsonData.encoding.content[1]
            val multiChainEncryption = MultiChainEncryption.from(encryptionTypeRaw)

            val name = jsonData.meta.name

            return ImportAccountMeta(name, networkType, multiChainEncryption)
        } catch (_: Exception) {
            throw InvalidJsonException()
        }
    }

    fun decode(json: String, password: String): ImportAccountData {
        val jsonData = decodeJson(json)

        try {
            return decode(jsonData, password)
        } catch (exception: IncorrectPasswordException) {
            throw exception
        } catch (_: Exception) {
            throw InvalidJsonException()
        }
    }

    private fun decode(jsonData: JsonAccountData, password: String): ImportAccountData {
        val username = jsonData.meta.name

        val networkTypeIdentifier = getNetworkTypeIdentifier(
            jsonData.address,
            jsonData.meta.genesisHash
        )

        val byteData = Base64.decode(jsonData.encoded)

        val typeDecoder = TypeCoderFactory.getDecoder(jsonData.encoding.type)
            ?: throw InvalidJsonException()

        val secret = typeDecoder.decode(byteData, password.encodeToByteArray())
            ?: throw IncorrectPasswordException()

        val contentDecoder = ContentCoderFactory.getDecoder(jsonData.encoding.content)
            ?: throw InvalidJsonException()

        val decodedSecret = contentDecoder.decode(secret)

        return ImportAccountData(
            decodedSecret.keypair,
            decodedSecret.multiChainEncryption,
            username,
            networkTypeIdentifier,
            decodedSecret.seed
        )
    }

    private fun getNetworkTypeIdentifier(
        address: String?,
        genesisHash: String?
    ): NetworkTypeIdentifier {
        val addressByte = address?.addressByteOrNull()

        return when {
            genesisHash != null -> NetworkTypeIdentifier.Genesis(genesisHash)
            addressByte != null -> NetworkTypeIdentifier.AddressByte(addressByte)
            else -> NetworkTypeIdentifier.Undefined
        }
    }

    private fun decodeJson(json: String): JsonAccountData {
        return try {
            gson.fromJson(json, JsonAccountData::class.java)
        } catch (exception: Exception) {
            throw InvalidJsonException()
        }
    }
}
