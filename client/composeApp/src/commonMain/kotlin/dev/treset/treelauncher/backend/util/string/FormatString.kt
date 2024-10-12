package dev.treset.treelauncher.backend.util.string

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
            val modA = a.lowercase(Locale.getDefault())
            val modB = b.lowercase(Locale.getDefault())
            val costs = IntArray(modB.length + 1)
            for (j in costs.indices) costs[j] = j
            for (i in 1..modA.length) {
                costs[0] = i
                var nw = i - 1
                for (j in 1..modB.length) {
                    val cj = min(
                        (1 + min(costs[j].toDouble(), costs[j - 1].toDouble())),
                        (if (modA[i - 1] == modB[j - 1]) nw else nw + 1).toDouble()
                    ).toInt()
                    nw = costs[j]
                    costs[j] = cj
                }
            }
            return costs[modB.length]
        }
    }
}

operator fun String.times(i: Int): String {
    if(i <= 0) {
        throw IllegalStateException("Cannot multiply a string by a non-positive number")
    }

    val sb = StringBuilder()
    for(j in 1..i) {
        sb.append(this)
    }
    return sb.toString()
}
