package dev.treset.treelauncher.backend.data.manifest

import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.LauncherLaunchArgument
import dev.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class VersionComponent(
    id: String,
    name: String,
    var versionNumber: String,
    var versionType: String,
    var loaderVersion: String?,
    var assets: String?,
    var virtualAssets: String?,
    var natives: String?,
    var depends: String?,
    var gameArguments: List<LauncherLaunchArgument>,
    var jvmArguments: List<LauncherLaunchArgument>,
    var java: String?,
    var libraries: List<String>,
    var mainClass: String,
    var mainFile: String?,
    var versionId: String,
    file: LauncherFile,
    type: LauncherManifestType = LauncherManifestType.VERSION_COMPONENT,
    includedFiles: Array<String> = appConfig().versionDefaultIncludedFiles,
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
    override fun copyData(other: Component) {
        super.copyData(other)

        if (other is VersionComponent) {
            other.versionNumber = versionNumber
            other.versionType = versionType
            other.loaderVersion = loaderVersion
            other.assets = assets
            other.virtualAssets = virtualAssets
            other.natives = natives
            other.depends = depends
            other.gameArguments = gameArguments
            other.jvmArguments = jvmArguments
            other.java = java
            other.libraries = libraries
            other.mainClass = mainClass
        }
    }

    companion object {
        @Throws(IOException::class)
        fun readFile(file: LauncherFile): VersionComponent {
            return readFile(
                file,
                LauncherManifestType.VERSION_COMPONENT,
            )
        }
    }
}