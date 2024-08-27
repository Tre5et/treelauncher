package net.treset.treelauncher.backend.data

import net.treset.mc_version_loader.exception.FileDownloadException
import net.treset.mc_version_loader.json.SerializationException
import net.treset.mc_version_loader.mods.CombinedModData
import net.treset.mc_version_loader.mods.MinecraftMods
import net.treset.mc_version_loader.mods.ModData
import net.treset.mc_version_loader.mods.curseforge.CurseforgeMod
import net.treset.mc_version_loader.mods.modrinth.ModrinthMod
import net.treset.mc_version_loader.util.FileUtil
import net.treset.mc_version_loader.util.Sources

class LauncherMod(
    var currentProvider: String?,
    var description: String?,
    var isEnabled: Boolean,
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
            if (MinecraftMods.getModrinthUserAgent() == null || MinecraftMods.getCurseforgeApiKey().isBlank()) {
                throw FileDownloadException("Modrinth user agent or curseforge api key not set")
            }
            val mods = ArrayList<ModData>()
            for (download in downloads) {
                if (download.provider == "modrinth") {
                    val json = FileUtil.getStringFromHttpGet(
                        Sources.getModrinthProjectUrl(download.id),
                        Sources.getModrinthHeaders(MinecraftMods.getModrinthUserAgent()),
                        listOf()
                    )
                    if (json == null || json.isBlank()) {
                        continue
                    }
                    var modrinthMod: ModrinthMod
                    try {
                        modrinthMod = ModrinthMod.fromJson(json)
                    } catch (e: SerializationException) {
                        throw FileDownloadException("Could not parse modrinth mod data: $json", e)
                    }
                    if (modrinthMod.name != null && !modrinthMod.name.isBlank()) {
                        mods.add(modrinthMod)
                    }
                }
                if (download.provider == "curseforge") {
                    val json = FileUtil.getStringFromHttpGet(
                        Sources.getCurseforgeProjectUrl(
                            download.id.toInt().toLong()
                        ), Sources.getCurseforgeHeaders(MinecraftMods.getCurseforgeApiKey()), listOf()
                    )
                    if (json == null || json.isBlank()) {
                        continue
                    }
                    var curseforgeMod: CurseforgeMod
                    try {
                        curseforgeMod = CurseforgeMod.fromJson(json)
                    } catch (e: SerializationException) {
                        throw FileDownloadException("Could not parse curseforge mod data: $json", e)
                    }
                    if (curseforgeMod.name != null && !curseforgeMod.name.isBlank()) {
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
