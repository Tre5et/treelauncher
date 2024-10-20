package dev.treset.treelauncher.backend.data

import com.google.gson.annotations.SerializedName
import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.mods.CombinedModData
import dev.treset.mcdl.mods.ModData
import dev.treset.mcdl.mods.ModsDL
import dev.treset.mcdl.mods.curseforge.CurseforgeMod
import dev.treset.mcdl.mods.modrinth.ModrinthMod
import kotlinx.serialization.Serializable

@Serializable
class LauncherMod(
    var currentProvider: String?,
    var description: String?,
    @SerializedName("enabled", alternate = ["is_enabled"])
    var enabled: Boolean,
    var url: String?,
    var iconUrl: String?,
    var name: String,
    var fileName: String,
    var version: String,
    var downloads: List<LauncherModDownload>
) {
    @get:Throws(FileDownloadException::class)
    val modData: ModData
        get() {
            if (ModsDL.getModrinthUserAgent() == null || ModsDL.getCurseforgeApiKey().isBlank()) {
                throw FileDownloadException("Modrinth user agent or curseforge api key not set")
            }
            val mods = ArrayList<ModData>()
            for (download in downloads) {
                if (download.provider == "modrinth") {
                    val modrinthMod = ModrinthMod.get(download.id)
                    if (modrinthMod.name != null && modrinthMod.name.isNotBlank()) {
                        mods.add(modrinthMod)
                    }
                }
                if (download.provider == "curseforge") {
                    val curseforgeMod = CurseforgeMod.get(download.id.toLong())
                    if (curseforgeMod.name != null && curseforgeMod.name.isNotBlank()) {
                        mods.add(curseforgeMod)
                    }
                }
            }
            if (mods.isEmpty()) {
                throw FileDownloadException("No mod data found: mod=$name")
            }
            if (mods.size == 1) {
                return mods[0]
            }
            return CombinedModData(mods[0], mods[1])
        }
}
