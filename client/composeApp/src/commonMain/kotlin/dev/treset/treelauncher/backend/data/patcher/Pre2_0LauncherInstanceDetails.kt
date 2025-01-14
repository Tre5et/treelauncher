package dev.treset.treelauncher.backend.data.patcher

import dev.treset.mcdl.format.FormatUtils
import dev.treset.mcdl.json.GenericJsonParsable
import dev.treset.mcdl.json.SerializationException
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.LauncherFeature
import dev.treset.treelauncher.backend.data.LauncherLaunchArgument
import dev.treset.treelauncher.backend.data.manifest.InstanceComponent
import dev.treset.treelauncher.backend.util.file.LauncherFile
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
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
        features = second.features,
        jvmArguments = second.jvmArguments,
        ignoredFiles = second.ignoredFiles,
        totalTime = second.totalTime
    )
}