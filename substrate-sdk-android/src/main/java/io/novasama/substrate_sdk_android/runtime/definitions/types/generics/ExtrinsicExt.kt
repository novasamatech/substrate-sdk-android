package io.novasama.substrate_sdk_android.runtime.definitions.types.generics

import io.novasama.substrate_sdk_android.encrypt.EncryptionType
import io.novasama.substrate_sdk_android.runtime.definitions.types.composite.DictEnum
import io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension.extensions.verifySignature.VerifySignature
import java.util.Locale

class MultiSignature(val encryptionType: EncryptionType, val value: ByteArray)

fun Extrinsic.Instance.signatureInstance(): Any? {
    return when (type) {
        Extrinsic.ExtrinsicType.Bare -> null

        is Extrinsic.ExtrinsicType.GeneralTransaction -> type.findSignatureInExplicits()

        is Extrinsic.ExtrinsicType.Signed -> type.signature
    }
}

fun Extrinsic.Instance.signerIdentifier(): Any? {
    return when (val type = type) {
        Extrinsic.ExtrinsicType.Bare -> null

        is Extrinsic.ExtrinsicType.GeneralTransaction -> type.findSignerIdentifierInExplicits()

        is Extrinsic.ExtrinsicType.Signed -> type.accountIdentifier
    }
}

fun Extrinsic.Instance.explicits(): ExtrinsicPayloadExtrasInstance? {
    return when (val type = type) {
        Extrinsic.ExtrinsicType.Bare -> null

        is Extrinsic.ExtrinsicType.GeneralTransaction -> type.extensionExplicits

        is Extrinsic.ExtrinsicType.Signed -> type.signedExtras
    }
}

private fun Extrinsic.ExtrinsicType.GeneralTransaction.findSignerIdentifierInExplicits(): Any? {
    val explicit = extensionExplicits[VerifySignature.ID]
    return VerifySignature.getSignatureFromExplicit(explicit)?.account
}

fun Extrinsic.Instance.tryExtractMultiSignature(): MultiSignature? {
    val signature = signatureInstance() ?: return null

    val enumEntry = signature as? DictEnum.Entry<*> ?: return null
    val value = enumEntry.value as? ByteArray ?: return null

    val encryptionType =
        EncryptionType.fromStringOrNull(enumEntry.name.toLowerCase()) ?: return null

    return MultiSignature(encryptionType, value)
}

private fun Extrinsic.ExtrinsicType.GeneralTransaction.findSignatureInExplicits(): Any? {
    val explicit = extensionExplicits[VerifySignature.ID]
    return VerifySignature.getSignatureFromExplicit(explicit)?.signature
}

private val EncryptionType.multiSignatureName
    get() = rawName.capitalize(Locale.ROOT)

fun MultiSignature.prepareForEncoding(): Any {
    return DictEnum.Entry(encryptionType.multiSignatureName, value)
}

fun multiAddressFromId(addressId: ByteArray): DictEnum.Entry<ByteArray> {
    return DictEnum.Entry(
        name = MULTI_ADDRESS_ID,
        value = addressId
    )
}
