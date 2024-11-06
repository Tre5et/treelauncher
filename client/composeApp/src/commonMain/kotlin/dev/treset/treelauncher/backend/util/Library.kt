package dev.treset.treelauncher.backend.util

import kotlin.jvm.Throws

fun String.librarySame(other: String): Boolean {
    try {
        val l1 = this.extractNameVersionFromLibraryPath()
        val l2 = other.extractNameVersionFromLibraryPath()
        return l1.first == l2.first
    } catch (_: IllegalArgumentException) {
        return false
    }
}

fun String.libraryContained(libs: Collection<String>): Boolean {
    return libs.any { this.librarySame(it) }
}

@Throws(IllegalArgumentException::class)
fun String.extractNameVersionFromLibraryPath(): Pair<String, String> {
    val parts = split("[/\\\\]".toRegex())
    val numberIndex = parts.size - 2
    if(numberIndex < 0 || !parts[numberIndex].first().isDigit()) {
        throw IllegalArgumentException("No version number found")
    }
    val versionNumber = parts[numberIndex]
    val name = parts[parts.size - 1]
        .replace(versionNumber, "")
        .replace("--", "-")
        .replace("__", "_")
    return name to versionNumber
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