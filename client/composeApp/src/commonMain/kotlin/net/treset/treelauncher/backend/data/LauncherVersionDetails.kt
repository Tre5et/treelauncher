package net.treset.treelauncher.backend.data

import net.treset.mc_version_loader.json.GenericJsonParsable
import net.treset.mc_version_loader.json.SerializationException

class LauncherVersionDetails(
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
        fun fromJson(json: String?): LauncherVersionDetails {
            return fromJson(json, LauncherVersionDetails::class.java)
        }
    }
}