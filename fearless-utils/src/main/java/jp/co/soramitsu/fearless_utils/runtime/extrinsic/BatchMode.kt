package jp.co.soramitsu.fearless_utils.runtime.extrinsic

enum class BatchMode {

    /**
     * Execute all calls until finding an uncessffull one
     */
    BATCH,

    /**
     * Either execute all calls succeffuly or revert whole batch if at least one call was uncescessfull
     */
    BATCH_ALL,

    /**
     * Execute all successfull calls even if there was some unsuccessfull ones in between them
     */
    FORCE_BATCH
}
