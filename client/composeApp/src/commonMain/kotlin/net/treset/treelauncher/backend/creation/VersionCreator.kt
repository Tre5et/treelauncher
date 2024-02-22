package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.launcher.LauncherLaunchArgument
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherManifestType
import net.treset.mc_version_loader.minecraft.MinecraftLaunchArgument
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.util.exception.ComponentCreationException

open class VersionCreator(
    id: String,
    typeConversion: Map<String, LauncherManifestType>,
    componentsManifest: LauncherManifest
) : GenericComponentCreator(
    LauncherManifestType.VERSION_COMPONENT,
    null,
    null,
    id,
    typeConversion,
    null,
    appConfig().versionsDefaultDetails,
    componentsManifest
) {
    @Throws(ComponentCreationException::class)
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
        result.addAll(defaultArgs)
        LOGGER.debug { "Translated arguments: $result" }
        return result
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
