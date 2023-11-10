package net.treset.treelauncher.updater

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

@Serializable
data class UpdateChange(
    val path: String,
    val mode: Mode,
    val elements: List<Element>? = null
)

@Serializable
enum class Mode {
    FILE,
    DELETE,
    REGEX,
    LINE
}

@Serializable
data class Element(
    val pattern: String? = null,
    val value: String? = null,
    val meta: Int? = null,
    val replace: Boolean? = null
)

lateinit var update: List<UpdateChange>

@Throws(IOException::class)
fun readUpdate() {
    val file = File("update.json")
    if(!file.isFile) {
        throw IOException("File not found")
    }
    val json = file.readText()
    try {
        update = Json.decodeFromString<Array<UpdateChange>>(json).toList()
    } catch (e: Exception) {
        throw IOException("Unexpected json syntax: $e")
    }
}