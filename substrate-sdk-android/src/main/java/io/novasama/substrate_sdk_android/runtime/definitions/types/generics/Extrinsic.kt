@file:Suppress("EXPERIMENTAL_API_USAGE") // unsigned types

package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.definitions.types.errors.EncodeDecodeException
import io.novasama.substrate_sdk_android.runtime.definitions.types.useScaleWriter
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.TransactionExtension
import io.novasama.substrate_sdk_android.scale.dataType.byte
import io.novasama.substrate_sdk_android.scale.dataType.compactInt
import io.novasama.substrate_sdk_android.scale.utils.directWrite
import kotlin.experimental.and
import kotlin.experimental.or

private const val VERSION_MASK: Byte = 0b0011_1111
private const val TYPE_MASK: Byte = 0b1100_0000.toByte()

private const val TYPE_BARE: Byte = 0b00000000
private const val TYPE_SIGNED: Byte = 0b10000000.toByte()
private const val TYPE_GENERAL: Byte = 0b01000000

private const val LEGACY_EXTRINSIC_FORMAT_VERSION: Byte = 4
private const val EXTRINSIC_FORMAT_VERSION: Byte = 5

private const val TYPE_ADDRESS = "Address"
private const val TYPE_SIGNATURE = "ExtrinsicSignature"

/**
 * A type that encodes Substrate extrinsics. It supports extrinsics of version 4 and 5
 *
 * For v4 the layout is:
 *     Signed: version_byte | address | signature | transaction_extensions | call
 *     Unsigned: version_byte | call
 *
 * For v5 the layout is:
 *    Bare: version_byte | call
 *    General: version_byte | extensions_version | transaction_extensions_for(extensions_version) | call
 *
 * Note that for v5 General transactions encoded extensions depend on used extensions_version, providing versioning.
 * The set of extensions is resolved via [io.novasama.substrate_sdk_android.runtime.metadata.ExtrinsicMetadata.transactionExtensions]
 *
 * For more deatails about transaction extensions @see [TransactionExtension] docs
 */
object Extrinsic : Type<Extrinsic.Instance>("ExtrinsicsDecoder") {

    class Instance(
        val type: ExtrinsicType,
        val call: GenericCall.Instance
    )

    sealed class ExtrinsicType {

        object Bare : ExtrinsicType()

        class Signed(
            val accountIdentifier: Any?,
            val signature: Any?,
            val signedExtras: ExtrinsicPayloadExtrasInstance
        ) : ExtrinsicType()

        class GeneralTransaction(
            val extensionsVersion: Byte,
            val extensionExplicits: ExtrinsicPayloadExtrasInstance
        ) : ExtrinsicType()
    }

    fun signatureType(runtime: RuntimeSnapshot): Type<*> {
        return runtime.typeRegistry[TYPE_SIGNATURE]
            ?: requiredTypeNotFound(TYPE_SIGNATURE)
    }

    override val isFullyResolved: Boolean = true

    override fun decode(
        scaleCodecReader: ScaleCodecReader,
        runtime: RuntimeSnapshot
    ): Instance {
        val length = compactInt.read(scaleCodecReader)

        val extrinsicVersionByte = byte.read(scaleCodecReader)

        val extrinsicVersion = extrinsicVersionByte and VERSION_MASK
        val extrinsicType = extrinsicVersionByte and TYPE_MASK

        val type = when {
            extrinsicType == TYPE_BARE && versionSupportsBare(extrinsicVersion) -> ExtrinsicType.Bare

            extrinsicType == TYPE_SIGNED && extrinsicVersion == LEGACY_EXTRINSIC_FORMAT_VERSION -> {
                decodeSignedType(runtime, scaleCodecReader)
            }

            extrinsicType == TYPE_GENERAL && extrinsicVersion == EXTRINSIC_FORMAT_VERSION -> {
                decodeGeneralType(runtime, scaleCodecReader)
            }

            else -> error("Unknown extrinsic version: $extrinsicVersionByte")
        }

        val call = GenericCall.decode(scaleCodecReader, runtime)

        return Instance(type, call)
    }

    override fun encode(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: Instance
    ) {
        val noLengthBytes = encodeNoLength(runtime, value)
        Bytes.encode(scaleCodecWriter, runtime, noLengthBytes)
    }

    private fun versionSupportsBare(version: Byte): Boolean {
        return version == EXTRINSIC_FORMAT_VERSION || version == LEGACY_EXTRINSIC_FORMAT_VERSION
    }

    private fun decodeSignedType(
        runtime: RuntimeSnapshot,
        scaleCodecReader: ScaleCodecReader,
    ): ExtrinsicType.Signed {
        return ExtrinsicType.Signed(
            accountIdentifier = addressType(runtime).decode(scaleCodecReader, runtime),
            signature = signatureType(runtime).decode(scaleCodecReader, runtime),
            signedExtras = ExtrasIncludedInExtrinsic.decode(scaleCodecReader, runtime)
        )
    }

    private fun decodeGeneralType(
        runtime: RuntimeSnapshot,
        scaleCodecReader: ScaleCodecReader
    ): ExtrinsicType.GeneralTransaction {
        val extensionsVersion = scaleCodecReader.readByte()
        val explicits = ExtrasIncludedInExtrinsic.decode(scaleCodecReader, runtime, extensionsVersion)

        return ExtrinsicType.GeneralTransaction(extensionsVersion, explicits)
    }

    fun encodeWithoutLength(
        scaleCodecWriter: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        value: Instance
    ) {
        val noLengthBytes = encodeNoLength(runtime, value)
        scaleCodecWriter.directWrite(noLengthBytes)
    }

    private fun encodeNoLength(
        runtime: RuntimeSnapshot,
        value: Instance,
    ): ByteArray {
        return useScaleWriter {
            val writer = this@useScaleWriter

            encodeTransactionType(writer, runtime, value.type)
            GenericCall.encode(writer, runtime, value.call)
        }
    }

    private fun encodeTransactionType(
        writer: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        type: ExtrinsicType
    ) {
        writer.writeByte(type.versionByte(runtime))

        when (type) {
            // Nothing to encode
            ExtrinsicType.Bare -> Unit

            is ExtrinsicType.GeneralTransaction -> encodeGeneral(writer, runtime, type)

            is ExtrinsicType.Signed -> encodeSigned(writer, runtime, type)
        }
    }

    private fun encodeSigned(
        writer: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        signedType: ExtrinsicType.Signed,
    ) {
        addressType(runtime).encodeUnsafe(writer, runtime, signedType.accountIdentifier)
        signatureType(runtime).encodeUnsafe(writer, runtime, signedType.signature)
        ExtrasIncludedInExtrinsic.encodeUnsafe(writer, runtime, signedType.signedExtras)
    }

    private fun encodeGeneral(
        writer: ScaleCodecWriter,
        runtime: RuntimeSnapshot,
        generalType: ExtrinsicType.GeneralTransaction,
    ) {
        writer.writeByte(generalType.extensionsVersion)

        ExtrasIncludedInExtrinsic.encode(writer, runtime, generalType.extensionExplicits, generalType.extensionsVersion)
    }

    override fun isValidInstance(instance: Any?): Boolean {
        return instance is Instance
    }

    private fun ExtrinsicType.versionByte(runtime: RuntimeSnapshot): Byte {
        return when (this) {
            ExtrinsicType.Bare -> runtime.metadata.extrinsic.latestVersion.toByte() or TYPE_BARE

            is ExtrinsicType.GeneralTransaction -> EXTRINSIC_FORMAT_VERSION or TYPE_GENERAL

            is ExtrinsicType.Signed -> LEGACY_EXTRINSIC_FORMAT_VERSION or TYPE_SIGNED
        }
    }

    private fun addressType(runtime: RuntimeSnapshot): Type<*> {
        return runtime.typeRegistry[TYPE_ADDRESS]
            ?: requiredTypeNotFound(TYPE_ADDRESS)
    }

    private fun requiredTypeNotFound(name: String): Nothing {
        throw EncodeDecodeException("Cannot resolve $name type, which is required to work with Extrinsic")
    }
}

fun Extrinsic.toBytesWithoutLength(runtime: RuntimeSnapshot, value: Extrinsic.Instance): ByteArray {
    return useScaleWriter { encodeWithoutLength(this, runtime, value) }
}
