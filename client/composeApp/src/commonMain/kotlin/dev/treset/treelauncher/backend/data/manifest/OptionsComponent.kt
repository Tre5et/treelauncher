package dev.treset.treelauncher.backend.data.manifest

import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class OptionsComponent(
    id: String,
    name: String,
    file: LauncherFile,
    includedFiles: Array<String> = appConfig().optionsDefaultIncludedFiles,
    lastUsed: String = "",
    type: LauncherManifestType = LauncherManifestType.OPTIONS_COMPONENT,
    active: Boolean = false
): Component(
    type,
    id,
    name,
    includedFiles,
    lastUsed,
    active,
    file
) {
    companion object {
        @Throws(IOException::class)
        fun readFile(file: LauncherFile): OptionsComponent {
            return readFile(
                file,
                LauncherManifestType.OPTIONS_COMPONENT,
            )
        }
    }
}