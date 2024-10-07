package net.treset.treelauncher.backend.data.manifest

import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.LauncherMod
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class ModsComponent(
    id: String,
    name: String,
    var types: List<String>?,
    var versions: List<String>?,
    file: LauncherFile,
    includedFiles: Array<String> = appConfig().modsDefaultIncludedFiles,
    lastUsed: String = "",
    active: Boolean = false,
    type: LauncherManifestType = LauncherManifestType.MODS_COMPONENT,
    var mods: MutableList<LauncherMod> = mutableListOf()
): Component(
    type,
    id,
    name,
    includedFiles,
    lastUsed,
    active,
    file
) {
    override fun copyData(other: Component) {
        super.copyData(other)

        if (other is ModsComponent) {
            other.types = types
            other.versions = versions
            other.mods = mods
        }
    }

    companion object {
        @Throws(IOException::class)
        fun readFile(file: LauncherFile): ModsComponent {
            return readFile(
                file,
                LauncherManifestType.MODS_COMPONENT,
            )
        }
    }
}