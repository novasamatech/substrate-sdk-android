package jp.co.soramitsu.fearless_utils.decoratable_api.tx

interface DecoratableFunctions {

    val decorator: Decorator

    interface Decorator {

        fun function0(name: String): Function0

        fun <A1> function1(name: String): Function1<A1>

        fun <A1, A2> function2(name: String): Function2<A1, A2>
    }
}
