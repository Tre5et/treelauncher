package dev.treset.treelauncher.backend.data

import dev.treset.mcdl.mods.LocalModVersion
import dev.treset.treelauncher.backend.data.manifest.LauncherMod
import dev.treset.treelauncher.backend.util.file.LauncherFile

fun LocalModVersion.toLauncherMod(): LauncherMod {
    return LauncherMod(
        currentProvider = this.activeProvider.toString(),
        description = this.version.parentMod.description,
        enabled = true,
        url = this.version.parentMod.url,
        iconUrl = this.version.parentMod.iconUrl,
        name = this.version.parentMod.name,
        version = this.version.versionNumber,
        downloads = this.downloads.map {
            LauncherModDownload(it.provider.toString(), it.id)
        },
        file = LauncherFile.of(this.fileName).let {
            it.renamed("${it.nameWithoutExtension}.json")
        }
    )
}