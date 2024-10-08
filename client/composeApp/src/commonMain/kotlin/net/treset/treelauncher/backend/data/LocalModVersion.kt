package net.treset.treelauncher.backend.data

import net.treset.mc_version_loader.mods.LocalModVersion

fun LocalModVersion.toLauncherMod(): LauncherMod {
    return LauncherMod(
        currentProvider = this.activeProvider.toString(),
        description = this.version.parentMod.description,
        isEnabled = true,
        url = this.version.parentMod.url,
        iconUrl = this.version.parentMod.iconUrl,
        name = this.version.parentMod.name,
        fileName = this.fileName,
        version = this.version.versionNumber,
        downloads = this.downloads.map {
            LauncherModDownload(it.provider.toString(), it.id)
        }
    )
}