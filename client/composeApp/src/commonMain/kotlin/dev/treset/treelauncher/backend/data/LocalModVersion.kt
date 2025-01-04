package dev.treset.treelauncher.backend.data

import dev.treset.mcdl.mods.LocalModVersion
import dev.treset.treelauncher.backend.data.manifest.LauncherMod
import dev.treset.treelauncher.backend.util.assignFrom
import dev.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException
import kotlin.jvm.Throws

fun LocalModVersion.toLauncherMod(directory: LauncherFile): LauncherMod {
    val local = this
    return LauncherMod.rawFile(LauncherFile.of(this.fileName), directory).apply {
        currentProvider.value = local.activeProvider.toString()
        description.value = local.version.parentMod.description
        enabled.value = true
        url.value = local.version.parentMod.url
        iconUrl.value = local.version.parentMod.iconUrl
        name.value = local.version.parentMod.name
        version.value = local.version.versionNumber
        downloads.assignFrom(local.downloads.map {
            LauncherModDownload(it.provider.toString(), it.id)
        })
    }
}

@Throws(IOException::class)
fun LocalModVersion.updateModWith(mod: LauncherMod, directory: LauncherFile, preserveEnabledState: Boolean = true) {
    mod.setImportingMod(LauncherFile.of(this.fileName), directory, preserveEnabledState)
}