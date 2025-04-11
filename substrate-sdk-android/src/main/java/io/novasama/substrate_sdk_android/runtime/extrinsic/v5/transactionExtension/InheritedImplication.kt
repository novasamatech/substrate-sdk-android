package io.novasama.substrate_sdk_android.runtime.extrinsic.v5.transactionExtension

import io.novasama.substrate_sdk_android.runtime.RuntimeSnapshot
import io.novasama.substrate_sdk_android.runtime.definitions.types.generics.GenericCall
import io.novasama.substrate_sdk_android.runtime.extrinsic.ExtrinsicVersion
import io.novasama.substrate_sdk_android.runtime.metadata.TransactionExtensionMetadata

/**
 * Inherited implication is basically an accumulator used for transactions extensions to get a sense of
 * future extensions data. It us an important part of the pipeline since extensions cannot communicate with each other by default
 * and process the information independently, sequentially, in order.
 *
 * For each transaction tx, there is a set of transaction extensions, tx_extensions:
 * tx_extensions = [ext1, ext2, ..., extN]
 *
 * There are two different phases in which this list is processed:
 *
 * 1. Extrinsic construction
 *
 * For extrinsic construction, extensions are processed from the end to the start:
 *
 * extN -> ext[N-1] -> ... -> ext2 -> ext1
 *
 * At each processing step, inherited implication is construct as the following:
 *
 * inherited_implication(k) = extensions_version | call | explicits_k | implicits_k
 * explicits_k = (explicit[k+1], explicit[k+2], ..., explicitN])
 * implicits_k = (implicit[k+1], implicit[K+2], ..., implicitsN)
 *
 * So when processing some transaction extension k we include all information from all succeeding extensions
 * This is available in [succeedingExtensions] field
 *
 * At each step, we take an implicit (does not depend on any external data)
 * and explicit (might depend on current inherited implication) and append them to the inherited implication
 * to move to the next step
 * After all extensions are processed, inherited implication contains all information to encode & send transaction
 *
 * 2. Transaction validation
 *
 * Validation happens on the node and is performed from start to end:
 * ext1 -> ext2 -> ... -> ext[N-1] -> extN
 *
 */
interface InheritedImplication {

    val runtime: RuntimeSnapshot

    val extrinsicVersion: ExtrinsicVersion

    val call: GenericCall.Instance

    val succeedingExtensions: List<SucceedingExtensionValues>

    fun encoded(): ByteArray

    fun encodedExtensions(): ByteArray

    fun encodedImplicits(): ByteArray

    fun encodedExplicits(): ByteArray

    fun encodedCall(): ByteArray
}

class SucceedingExtensionValues(
    val transactionExtension: TransactionExtension,
    val extensionMetadata: TransactionExtensionMetadata,
    val implicit: Any?,
    val explicit: Any?,
    val nestingLevel: Int
)
