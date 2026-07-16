package io.novasama.substrate_sdk_android.runtime.metadata.module

import io.novasama.substrate_sdk_android.runtime.definitions.types.Type
import io.novasama.substrate_sdk_android.runtime.metadata.WithName

/**
 * A pallet view function (metadata v16+).
 *
 * View functions are dispatched via the `RuntimeViewFunction_execute_view_function` runtime api,
 * which is keyed by the globally-unique [id] rather than by pallet/name.
 */
class ViewFunction(
    override val name: String,
    /**
     * Globally-unique 32-byte identifier of the view function, used to dispatch it via the
     * `RuntimeViewFunction` runtime api.
     */
    val id: ByteArray,
    /**
     * Name of the pallet that declares this view function.
     */
    val palletName: String,
    val inputs: List<RuntimeApiMethodParam>,
    val output: Type<*>?,
) : WithName
