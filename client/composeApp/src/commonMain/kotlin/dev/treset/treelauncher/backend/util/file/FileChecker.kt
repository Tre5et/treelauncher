package dev.treset.treelauncher.backend.util.file

import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.relativeTo

typealias CheckFunction = (name: String) -> Boolean

fun StringCheckFunction(
    string: String
): CheckFunction = {
    it == string
}

fun AnyCheckFunction(): CheckFunction = { true }

open class FileChecker(
    private val function: (Path) -> Boolean
) {
    fun check(path: Path, relativeTo: Path? = null): Boolean {
        val finalPath = relativeTo?.let { path.relativeTo(it) } ?: path
        return function(finalPath)
    }

    companion object {}
}

class SimpleFileChecker(
    private val functions: List<CheckFunction>
): FileChecker(
    { path ->
        if(path.count() + 1 < functions.size) false
        var res = true
        for(p in path.withIndex()) {
            val n = p.value
            val i = p.index
            if(i > functions.lastIndex) break
            if(!functions[i](n.name)) {
                res = false
                break
            }
        }
        res
    }
) {
    constructor(vararg functions: (String) -> Boolean) : this(functions.toList())
}

class SimpleDirChecker(
    private val functions: List<CheckFunction>
): FileChecker(
    { path ->
        if(path.count() + 1 < functions.size) false
        var res = true
        for(p in path.withIndex()) {
            val n = p.value
            val i = p.index
            if(i > functions.lastIndex) break
            if(!functions[i](n.name)) {
                res = false
                break
            }
        }
        res
    }
) {
    constructor(vararg functions: (String) -> Boolean) : this(functions.toList())
}

class CombinedFileChecker(
    private val checkers: List<FileChecker>
) : FileChecker(
    { path ->
        if(checkers.isEmpty()) false
        checkers.all { it.check(path) }
    }
) {
    constructor(vararg checkers: FileChecker) : this(checkers.toList())
}