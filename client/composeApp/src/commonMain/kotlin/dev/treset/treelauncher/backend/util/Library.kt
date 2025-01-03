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
fun String.extractNameVersionFromJarFile(): Pair<String, String?> {
    return extractNameVersionFromFile(".jar")
}

fun String.extractNameVersionFromFile(vararg extensions: String): Pair<String, String?> {
    val extension = extensions.firstOrNull { endsWith(it) } ?: throw IllegalArgumentException("No matching file extension")
    val filePart = split("[/\\\\]".toRegex()).last()
    val firstNum = filePart.indexOfFirst { it.isDigit() }
    if(firstNum >= -1) {
        for(i in firstNum downTo 0) {
            if(filePart[i] == '-' || filePart[i] == '_' || filePart[i] == ' ' || filePart[i] == '.') {
                val number = filePart.substring(i + 1, filePart.length - extension.length)
                val name = filePart.substring(0, i)
                return name to number
            }
        }
    }
    return filePart to null
}