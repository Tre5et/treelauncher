package net.treset.treelauncher.backend.data.patcher

import net.treset.mcdl.format.FormatUtils
import net.treset.mcdl.json.GenericJsonParsable
import net.treset.mcdl.json.SerializationException
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.LauncherFeature
import net.treset.treelauncher.backend.data.LauncherLaunchArgument
import net.treset.treelauncher.backend.data.manifest.InstanceComponent
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.time.LocalDateTime

class Pre2_0LauncherInstanceDetails(
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
        fun fromJson(json: String?): Pre2_0LauncherInstanceDetails {
            return fromJson(json, Pre2_0LauncherInstanceDetails::class.java)
        }
    }
}

fun Pair<Pre2_0ComponentManifest, Pre2_0LauncherInstanceDetails>.toInstanceComponent(): InstanceComponent {
    return InstanceComponent(
        first.id,
        first.name,
        second.versionComponent,
        second.savesComponent,
        second.resourcepacksComponent,
        second.optionsComponent,
        second.modsComponent,
        LauncherFile.of(first.directory, appConfig().manifestFileName),
        lastUsed = second.lastPlayed ?: first.lastUsed ?: "",
        features = second.features.toTypedArray(),
        jvmArguments = second.jvmArguments.toTypedArray(),
        ignoredFiles = second.ignoredFiles.toTypedArray(),
        totalTime = second.totalTime
    )
}