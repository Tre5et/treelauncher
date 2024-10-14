package dev.treset.treelauncher.backend.data.manifest

import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.LauncherFeature
import dev.treset.treelauncher.backend.data.LauncherLaunchArgument
import dev.treset.treelauncher.backend.launching.resources.InstanceResourceProvider
import dev.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class InstanceComponent(
    id: String,
    name: String,
    var versionComponent: String,
    var savesComponent: String,
    var resourcepacksComponent: String,
    var optionsComponent: String,
    var modsComponent: String?,
    file: LauncherFile,
    active: Boolean = false,
    lastUsed: String = "",
    includedFiles: Array<String> = appConfig().instanceDefaultIncludedFiles,
    var features: Array<LauncherFeature> = appConfig().instanceDefaultFeatures,
    var jvmArguments: Array<LauncherLaunchArgument> = appConfig().instanceDefaultJvmArguments,
    var ignoredFiles: Array<String> = appConfig().instanceDefaultIgnoredFiles,
    var totalTime: Long = 0
): Component(
    LauncherManifestType.INSTANCE_COMPONENT,
    id,
    name,
    includedFiles,
    lastUsed,
    active,
    file
) {
    override fun getResourceProvider(gameDataDir: LauncherFile): InstanceResourceProvider {
        return InstanceResourceProvider(this, gameDataDir)
    }

    override fun copyData(other: Component) {
        super.copyData(other)

        if(other is InstanceComponent) {
            other.features = features
            other.ignoredFiles = ignoredFiles
            other.jvmArguments = jvmArguments
            other.modsComponent = modsComponent
            other.optionsComponent = optionsComponent
            other.resourcepacksComponent = resourcepacksComponent
            other.savesComponent = savesComponent
            other.versionComponent = versionComponent
            other.totalTime = totalTime
        }
    }

    companion object {
        @Throws(IOException::class)
        fun readFile(file: LauncherFile): InstanceComponent {
            return readFile(
                file,
                LauncherManifestType.INSTANCE_COMPONENT,
            )
        }
    }
}