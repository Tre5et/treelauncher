package net.treset.treelauncher.backend.data.patcher

import dev.treset.mcdl.json.GenericJsonParsable
import dev.treset.mcdl.json.SerializationException
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.LauncherLaunchArgument
import net.treset.treelauncher.backend.data.manifest.VersionComponent
import net.treset.treelauncher.backend.util.file.LauncherFile

class Pre2_0LauncherVersionDetails(
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
    var versionId: String
) : GenericJsonParsable() {
    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String?): Pre2_0LauncherVersionDetails {
            return fromJson(json, Pre2_0LauncherVersionDetails::class.java)
        }
    }
}

fun Pair<Pre2_0ComponentManifest, Pre2_0LauncherVersionDetails>.toVersionComponent(): VersionComponent {
    return VersionComponent(
        first.id,
        first.name,
        second.versionNumber,
        second.versionType,
        second.loaderVersion,
        second.assets,
        second.virtualAssets,
        second.natives,
        second.depends,
        second.gameArguments,
        second.jvmArguments,
        second.java,
        second.libraries,
        second.mainClass,
        second.mainFile,
        second.versionId,
        LauncherFile.of(first.directory, appConfig().manifestFileName),
        includedFiles = first.includedFiles.toTypedArray(),
        lastUsed = first.lastUsed ?: "",
    )
}