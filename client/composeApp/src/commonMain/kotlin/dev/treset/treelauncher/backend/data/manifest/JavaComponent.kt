package dev.treset.treelauncher.backend.data.manifest

import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class JavaComponent(
    id: String,
    name: String,
    file: LauncherFile,
    type: LauncherManifestType = LauncherManifestType.JAVA_COMPONENT,
    includedFiles: Array<String> = appConfig().javaDefaultIncludedFiles,
    lastUsed: String = "",
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
        fun readFile(file: LauncherFile): JavaComponent {
            return readFile(
                file,
                LauncherManifestType.JAVA_COMPONENT,
            )
        }
    }
}