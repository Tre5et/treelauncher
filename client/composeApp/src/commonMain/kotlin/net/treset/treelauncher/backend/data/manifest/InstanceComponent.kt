package net.treset.treelauncher.backend.data.manifest

import net.treset.treelauncher.backend.data.LauncherFeature
import net.treset.treelauncher.backend.data.LauncherLaunchArgument
import net.treset.treelauncher.backend.launching.resources.InstanceResourceProvider
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class InstanceComponent(
    type: LauncherManifestType,
    id: String,
    name: String,
    includedFiles: List<String>,
    lastUsed: String,
    active: Boolean,
    file: LauncherFile,
    var features: List<LauncherFeature>,
    var ignoredFiles: List<String>,
    var jvmArguments: List<LauncherLaunchArgument>,
    var modsComponent: String?,
    var optionsComponent: String,
    var resourcepacksComponent: String,
    var savesComponent: String,
    var versionComponent: String,
    var totalTime: Long = 0
): Component(
    type,
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