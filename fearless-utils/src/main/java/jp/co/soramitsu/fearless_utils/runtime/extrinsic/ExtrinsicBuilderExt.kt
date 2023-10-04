package jp.co.soramitsu.fearless_utils.runtime.extrinsic

import jp.co.soramitsu.fearless_utils.runtime.metadata.SignedExtensionId
import jp.co.soramitsu.fearless_utils.runtime.metadata.SignedExtensionValue

fun ExtrinsicBuilder.signedExtra(id: SignedExtensionId, value: Any?) {
    signedExtension(id, SignedExtensionValue(signedExtra = value))
}

fun ExtrinsicBuilder.additionalSigned(id: SignedExtensionId, value: Any?) {
    signedExtension(id, SignedExtensionValue(additionalSigned = value))
}