package net.treset.treelauncher.backend.data

import dev.treset.mcdl.json.GenericJsonParsable
import dev.treset.mcdl.json.JsonUtils
import dev.treset.mcdl.json.SerializationException

class LauncherModsDetails(
    var types: List<String>?,
    var versions: List<String>?,
    var mods: MutableList<LauncherMod>
) : GenericJsonParsable() {

    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String?): LauncherModsDetails {
            val details = fromJson(json, LauncherModsDetails::class.java)
            if (details.types == null) {
                val obj = JsonUtils.getAsJsonObject(JsonUtils.parseJson(json))
                val type = JsonUtils.getAsString(obj, "mods_type")
                details.types = listOf(type)
            }
            if (details.versions == null) {
                val obj = JsonUtils.getAsJsonObject(JsonUtils.parseJson(json))
                val version = JsonUtils.getAsString(obj, "mods_version")
                details.versions = listOf(version)
            }
            return details
        }
    }
}
