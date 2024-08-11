package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.minecraft.MinecraftLaunchArgument
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.data.LauncherLaunchArgument
import net.treset.treelauncher.backend.data.manifest.LauncherManifestType
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.util.exception.ComponentCreationException

abstract class VersionCreator(
    id: String,
    typeConversion: Map<String, LauncherManifestType>,
    componentsManifest: ParentManifest,
    var files: LauncherFiles,
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
    override fun createComponent(): String {
        for (v in files.versionComponents) {
            if (v.second.versionId.isNotBlank() && matchesVersion(v.second.versionId)) {
                LOGGER.debug { "Matching version already exists, using instead: versionId=${v.second.versionId}, usingId=${v.first.id}" }
                uses = v.first
                return useComponent()
            }
        }
        val result = super.createComponent()
        if (newManifest == null) {
            attemptCleanup()
            throw ComponentCreationException("Failed to create version component: invalid data")
        }
        makeVersion()
        LOGGER.debug { "Created version component: id=${newManifest!!.id}" }
        return result
    }

    @Throws(ComponentCreationException::class)
    override fun inheritComponent(): String {
        throw ComponentCreationException("Unable to inherit version: not supported")
    }

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
        for(a in defaultArgs) {
            if(result.none { it.argument == a.argument }) {
                result.add(a)
            }
        }
        LOGGER.debug { "Translated arguments: $result" }
        return result
    }

    protected abstract fun matchesVersion(id: String): Boolean

    @Throws(ComponentCreationException::class)
    protected abstract fun makeVersion()

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
