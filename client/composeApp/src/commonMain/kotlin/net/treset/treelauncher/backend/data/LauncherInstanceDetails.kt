package net.treset.treelauncher.backend.data

import dev.treset.mcdl.format.FormatUtils
import dev.treset.mcdl.json.GenericJsonParsable
import dev.treset.mcdl.json.SerializationException
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
