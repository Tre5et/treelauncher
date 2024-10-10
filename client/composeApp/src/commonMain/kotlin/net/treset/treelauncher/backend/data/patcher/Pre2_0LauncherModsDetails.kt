package net.treset.treelauncher.backend.data.patcher

import net.treset.mcdl.json.GenericJsonParsable
import net.treset.mcdl.json.JsonUtils
import net.treset.mcdl.json.SerializationException
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.LauncherMod
import net.treset.treelauncher.backend.data.manifest.ModsComponent
import net.treset.treelauncher.backend.util.file.LauncherFile

class Pre2_0LauncherModsDetails(
    var types: List<String>?,
    var versions: List<String>?,
    var mods: MutableList<LauncherMod>
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

fun Pair<Pre2_0ComponentManifest, Pre2_0LauncherModsDetails>.toModsComponent(): ModsComponent {
    return ModsComponent(
        first.id,
        first.name,
        second.types,
        second.versions,
        LauncherFile.of(first.directory, appConfig().manifestFileName),
        includedFiles = first.includedFiles.toTypedArray(),
        lastUsed = first.lastUsed ?: "",
        mods = second.mods
    )
}