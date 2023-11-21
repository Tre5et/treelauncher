package net.treset.treelauncher.backend.util.string

import java.util.*
import java.util.regex.Pattern

class PatternString(original: String, keep: Boolean = false) : FormatString() {
    private val pattern: String

    init {
        pattern =
            if (keep) {
                original
            } else {
                original.replace("\\\\".toRegex(), "\\\\\\\\")
                    .replace("(?<=^|[^\\\\])\\.(?=[^*+?]|$)".toRegex(), "\\\\.")
                    .let {
                        if (it.startsWith("^")) it else "^$it"
                    }.let {
                        if (it.endsWith("$")) it else "$it$"
                    }
            }
    }

    fun firstGroup(test: String?): String? {
        if (test == null) return null
        val matcher = Pattern.compile(get()).matcher(test)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }

    fun matches(test: String?): Boolean {
        return test?.matches(get().toRegex()) ?: false
    }

    // \ -> \\; . -> \.; [.*;.+;.?] unchanged; no .* at start / end -> ^ / $
    override fun get(): String {
        return pattern
    }

    companion object {
        fun toPattern(vararg items: String): Array<PatternString> {
            return items.map { original: String -> PatternString(original) }.toTypedArray()
        }

        fun decode(pattern: String): String {
            return pattern.replace("\\\\\\\\".toRegex(), "\\\\")
                .replace("\\\\\\.".toRegex(), ".")
                .replace("^\\^".toRegex(), "")
                .replace("\\$$".toRegex(), "")
        }

        fun decode(vararg patterns: String): Array<String> {
            return patterns.map { pattern: String -> decode(pattern) }.toTypedArray()
        }

        fun matchesAny(test: String, patterns: Array<PatternString>): Boolean {
            return patterns.any { p: PatternString -> p.matches(test) }
        }
    }
}
