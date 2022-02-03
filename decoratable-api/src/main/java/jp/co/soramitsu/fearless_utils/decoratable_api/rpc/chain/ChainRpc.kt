package jp.co.soramitsu.fearless_utils.decoratable_api.rpc.chain

import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.DecoratableRPC
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.DecoratableRPCModule
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.RpcCall0
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.RpcCall1
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.asJson
import jp.co.soramitsu.fearless_utils.decoratable_api.rpc.asString
import jp.co.soramitsu.fearless_utils.extensions.removeHexPrefix
import jp.co.soramitsu.fearless_utils.wsrpc.request.runtime.chain.RuntimeVersion
import java.math.BigInteger

interface ChainRpc : DecoratableRPCModule

val DecoratableRPC.chain: ChainRpc
    get() = decorate("chain") {
        object : ChainRpc, DecoratableRPCModule by this {}
    }

val ChainRpc.getFinalizedHead: RpcCall1<String?, String>
    get() = with(decorator) {
        call1("getFinalizedHead", asString)
    }

val ChainRpc.getBlockHash: RpcCall1<BigInteger?, String>
    get() = with(decorator) {
        call1("getBlockHash", asString)
    }

val ChainRpc.getHeader: RpcCall1<String?, SignedBlock.Block.Header>
    get() = with(decorator) {
        call1("getHeader", asJson())
    }

data class SignedBlock(val block: Block, val justification: Any?) {
    data class Block(val extrinsics: List<String>, val header: Header) {
        data class Header(private val number: String, val parentHash: String?) {

            fun parsedNumber(): Int = number.removeHexPrefix().toInt(radix = 16)
        }
    }
}

val ChainRpc.getRuntimeVersion: RpcCall0<RuntimeVersion>
    get() = with(decorator) {
        call0("getRuntimeVersion", asJson())
    }
