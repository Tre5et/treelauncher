package net.treset.treelauncher.backend.data

import net.treset.mc_version_loader.format.FormatUtils
import net.treset.mc_version_loader.json.GenericJsonParsable
import net.treset.mc_version_loader.json.SerializationException
import java.time.LocalDateTime

class LauncherInstanceDetails(
    var features: List<LauncherFeature>,
    var ignoredFiles: List<String>,
    var jvmArguments: List<LauncherLaunchArgument>,
    var modsComponent: String?,
    var optionsComponent: String,
    var resourcepacksComponent: String,
    var savesComponent: String,
    var versionComponent: String
) : GenericJsonParsable() {
    var lastPlayed: String? = null
    var totalTime: Long = 0

    private var lastPlayedTime: LocalDateTime?
        get() = FormatUtils.parseLocalDateTime(lastPlayed)
        set(lastPlayed) {
            this.lastPlayed = FormatUtils.formatLocalDateTime(lastPlayed)
        }

    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String?): LauncherInstanceDetails {
            return fromJson(json, LauncherInstanceDetails::class.java)
        }
    }
}
