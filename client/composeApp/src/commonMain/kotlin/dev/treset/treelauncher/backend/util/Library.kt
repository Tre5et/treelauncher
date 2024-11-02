package dev.treset.treelauncher.backend.util

import kotlin.jvm.Throws

fun String.librarySame(other: String): Boolean {
    try {
        val l1 = this.extractNameVersion()
        val l2 = other.extractNameVersion()
        return l1.first == l2.first
    } catch (e: IllegalArgumentException) {
        return false
    }
}

fun String.libraryContained(libs: Collection<String>): Boolean {
    return libs.any { this.librarySame(it) }
}

@Throws(IllegalArgumentException::class)
fun String.extractNameVersion(): Pair<String, String> {
    val parts = split("[/\\\\]".toRegex())
    val numberIndex = parts.size - 2
    val fileResult = this.extractNameVersionFromJarFile()
    val number = if(numberIndex < 0 || !parts[numberIndex].first().isDigit()) {
        fileResult.second
    } else parts[numberIndex]
    return fileResult.first to number
}

@Throws(IllegalArgumentException::class)
fun String.extractNameVersionFromJarFile(): Pair<String, String> {
    if(!endsWith(".jar")) {
        throw IllegalArgumentException("Not a jar file")
    }
    val filePart = split("[/\\\\]".toRegex()).last()
    val firstNum = filePart.indexOfFirst { it.isDigit() }
    if(firstNum >= -1) {
        for(i in firstNum downTo 0) {
            if(filePart[i] == '-' || filePart[i] == '_' || filePart[i] == ' ') {
                val number = filePart.substring(i + 1, filePart.length - 4)
                val name = filePart.substring(0, i)
                return name to number
            }
        }
    }
    throw IllegalArgumentException("No part split found")
}