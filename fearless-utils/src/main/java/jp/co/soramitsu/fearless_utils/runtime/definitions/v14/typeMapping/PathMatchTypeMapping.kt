package jp.co.soramitsu.fearless_utils.runtime.definitions.v14.typeMapping

import jp.co.soramitsu.fearless_utils.extensions.tryFindNonNull
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypePresetBuilder
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.getOrCreate
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.RuntimeType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.aliasedAs
import jp.co.soramitsu.fearless_utils.runtime.definitions.v14.typeMapping.PathMatchTypeMapping.Matcher
import jp.co.soramitsu.fearless_utils.runtime.metadata.v14.PortableType
import jp.co.soramitsu.fearless_utils.scale.EncodableStruct

typealias PathMatchTypeMappingReplacement<T> = Pair<T, PathMatchTypeMapping.Replacement>

class PathMatchTypeMapping(
    private val replacements: List<PathMatchTypeMappingReplacement<Matcher>>,
) : SiTypeMapping {

    sealed class Replacement {

        abstract fun create(suggestedName: String, types: TypePresetBuilder): RuntimeType<*, *>

        class AliasTo(private val aliasedName: String) : Replacement() {

            override fun create(
                suggestedName: String,
                types: TypePresetBuilder
            ): RuntimeType<*, *> {
                return types.getOrCreate(aliasedName).aliasedAs(suggestedName)
            }
        }

        class CreateType(
            private val createExpr: (suggestedName: String, types: TypePresetBuilder) -> RuntimeType<*, *>
        ) : Replacement() {

            override fun create(
                suggestedName: String,
                types: TypePresetBuilder
            ): RuntimeType<*, *> {
                return createExpr(suggestedName, types)
            }
        }
    }

    sealed class Matcher {

        abstract fun match(fullPathName: String): Boolean

        class Exact(val value: String) : Matcher() {
            override fun match(fullPathName: String): Boolean {
                return value == fullPathName
            }
        }

        class PrefixMatch(val prefix: String) : Matcher() {
            override fun match(fullPathName: String): Boolean {
                return fullPathName.startsWith(prefix)
            }
        }

        class SuffixMatch(val suffix: String) : Matcher() {
            override fun match(fullPathName: String): Boolean {
                return fullPathName.endsWith(suffix)
            }
        }
    }

    override fun map(
        originalDefinition: EncodableStruct<PortableType>,
        suggestedTypeName: String,
        typesBuilder: TypePresetBuilder
    ): Type<*>? {
        return replacements.tryFindNonNull { (matcher, replacement) ->
            if (matcher.match(suggestedTypeName)) {
                replacement.create(suggestedTypeName, typesBuilder)
            } else {
                null
            }
        }
    }
}

/**
 * Can be used to pass wildcard strings instead of [Matcher] instances
 *
 * "full.name" -> Matcher.Exact("full.name")
 * "*suffix.name" -> Matcher.Suffix("suffix.name")
 * "prefix.name*" -> Matcher.Prefix("prefix.name)
 *
 * Wildcards are only considered at the beginning or at the end of the string,
 * others will be considered as a part of the content
 */
fun PathMatchTypeMapping(
    vararg replacements: PathMatchTypeMappingReplacement<String>
): PathMatchTypeMapping {
    val replacementMatchers = replacements.map { (wildCardString, typeCreator) ->
        Matcher(wildCardString) to typeCreator
    }

    return PathMatchTypeMapping(replacementMatchers)
}

private fun Matcher(wildCardString: String): Matcher {
    return when {
        wildCardString.startsWith("*") -> Matcher.SuffixMatch(wildCardString.drop(1))
        wildCardString.endsWith("*") -> Matcher.PrefixMatch(wildCardString.dropLast(1))
        else -> Matcher.Exact(wildCardString)
    }
}
