import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.File
import java.io.IOException

data class UpdateChange(
    val path: String,
    val mode: Mode,
    val elements: List<Element>?
)

enum class Mode {
    FILE,
    DELETE,
    REGEX,
    LINE
}

data class Element(
    val pattern: String?,
    val value: String?,
    val meta: Int?,
    val replace: Boolean?
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
        update = Gson().fromJson(json, Array<UpdateChange>::class.java).toList()
    } catch (e: JsonSyntaxException) {
        throw IOException("Unexpected json syntax: $e")
    }
}