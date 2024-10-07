package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mcdl.minecraft.MinecraftLaunchArgument
import net.treset.treelauncher.backend.data.LauncherLaunchArgument
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.data.manifest.VersionComponent
import net.treset.treelauncher.backend.util.Status
import java.io.IOException

abstract class VersionCreator<D: VersionCreationData>(
    parentManifest: ParentManifest,
    onStatus: (Status) -> Unit
) : ComponentCreator<VersionComponent, D>(parentManifest, onStatus) {

    @Throws(IOException::class)
    override fun new(data: D): VersionComponent {
        data.currentVersions.firstOrNull { it.versionId == data.versionId }?.let {
            LOGGER.debug { "Matching version already exists, using instead: versionId=${data.versionId}" }
            return use(it)
        }
        return super.new(data)
    }

    @Throws(IOException::class)
    override fun inherit(component: VersionComponent, data: D): VersionComponent {
        throw IOException("Version inheritance not supported")
    }

    @Throws(IOException::class)
    override fun createInherit(data: D, statusProvider: CreationProvider): VersionComponent {
        throw IOException("Version inheritance not supported")
    }

    override val inheritTotal = -1

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
    val currentVersions: Array<VersionComponent>
): CreationData(name)
