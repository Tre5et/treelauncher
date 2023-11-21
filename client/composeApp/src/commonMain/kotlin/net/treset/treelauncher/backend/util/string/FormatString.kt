package net.treset.treelauncher.backend.util.string

import net.treset.treelauncher.backend.util.string.FormatString.FormatException
import java.util.*
import kotlin.math.min

abstract class FormatString {
    @Throws(FormatException::class)
    abstract fun get(): String
    override fun toString(): String {
        return try {
            get()
        } catch (e: FormatException) {
            ""
        }
    }

    class FormatException : Exception {
        constructor(message: String?) : super(message)
        constructor(message: String?, cause: Throwable?) : super(message, cause)
    }

    companion object {
        fun distance(a: String, b: String): Int {
            var a = a
            var b = b
            a = a.lowercase(Locale.getDefault())
            b = b.lowercase(Locale.getDefault())
            val costs = IntArray(b.length + 1)
            for (j in costs.indices) costs[j] = j
            for (i in 1..a.length) {
                costs[0] = i
                var nw = i - 1
                for (j in 1..b.length) {
                    val cj = min(
                        (1 + min(costs[j].toDouble(), costs[j - 1].toDouble())).toDouble(),
                        (if (a[i - 1] == b[j - 1]) nw else nw + 1).toDouble()
                    ).toInt()
                    nw = costs[j]
                    costs[j] = cj
                }
            }
            return costs[b.length]
        }
    }
}
