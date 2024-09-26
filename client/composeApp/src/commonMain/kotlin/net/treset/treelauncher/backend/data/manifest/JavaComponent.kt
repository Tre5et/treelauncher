package net.treset.treelauncher.backend.data.manifest

import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import java.io.IOException

class JavaComponent(
    id: String,
    name: String,
    file: LauncherFile,
    type: LauncherManifestType = LauncherManifestType.JAVA_COMPONENT,
    includedFiles: Array<PatternString> = emptyArray(),
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