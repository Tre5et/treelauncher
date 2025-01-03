package dev.treset.treelauncher.backend.data.patcher

import dev.treset.mcdl.json.GenericJsonParsable
import dev.treset.mcdl.json.JsonUtils
import dev.treset.mcdl.json.SerializationException
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.manifest.Pre3_1ModsComponent
import dev.treset.treelauncher.backend.util.file.LauncherFile

class Pre2_0LauncherModsDetails(
    var types: List<String>?,
    var versions: List<String>?,
    var mods: MutableList<Pre2_0LauncherMod>
) : GenericJsonParsable() {

    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String?): Pre2_0LauncherModsDetails {
            val details = fromJson(json, Pre2_0LauncherModsDetails::class.java)
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

fun Pair<Pre2_0ComponentManifest, Pre2_0LauncherModsDetails>.toPre3_1ModsComponent(): Pre3_1ModsComponent {
    return Pre3_1ModsComponent(
        first.id,
        first.name,
        second.types ?: emptyList(),
        second.versions ?: emptyList(),
        LauncherFile.of(first.directory, appConfig().manifestFileName),
        includedFiles = first.includedFiles,
        lastUsed = first.lastUsed ?: "",
        mods = second.mods.map { it.toPre3_1LauncherMod() }
    )
}