package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mcdl.minecraft.MinecraftLaunchArgument
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.data.LauncherLaunchArgument
import net.treset.treelauncher.backend.data.manifest.VersionComponent
import net.treset.treelauncher.backend.util.Status
import java.io.IOException

abstract class VersionCreator<D: VersionCreationData>(
    data: D,
    statusProvider: CreationProvider
) : NewComponentCreator<VersionComponent, D>(data, statusProvider) {
    constructor(
        data: D,
        onStatus: (Status) -> Unit
    ) : this(data, CreationProvider(null, 0, onStatus))

    @Throws(IOException::class)
    override fun create(): VersionComponent {
        data.files.versionComponents.firstOrNull { it.versionId == data.versionId }?.let {
            LOGGER.debug { "Matching version already exists, using instead: versionId=${data.versionId}" }
            return it
        }
        return super.create()
    }

    protected fun translateArguments(
        args: List<MinecraftLaunchArgument>,
        defaultArgs: Array<LauncherLaunchArgument>
    ): List<LauncherLaunchArgument> {
        val result: MutableList<LauncherLaunchArgument> = mutableListOf()
        for (a in args) {
            var feature: String? = null
            var osName: String? = null
            var osVersion: String? = null
            var osArch: String? = null
            if (a.isGated) {
                for (r in a.rules) {
                    r.features?.let { features ->
                        if (features.isHasCustomResolution) {
                            feature = "resolution_x"
                        } else if (features.isDemoUser) {
                            feature = "is_demo_user"
                        }
                    }
                    r.os?.let { os ->
                        osName = os.name
                        osVersion = os.version
                        osArch = os.arch
                    }
                }
            }
            result.add(LauncherLaunchArgument(a.name, feature, osName, osVersion, osArch))
        }
        for(a in defaultArgs) {
            if(result.none { it.argument == a.argument }) {
                result.add(a)
            }
        }
        LOGGER.debug { "Translated arguments: $result" }
        return result
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}

open class VersionCreationData(
    name: String,
    val versionId: String,
    val files: LauncherFiles
): NewCreationData(name, files.versionManifest)
