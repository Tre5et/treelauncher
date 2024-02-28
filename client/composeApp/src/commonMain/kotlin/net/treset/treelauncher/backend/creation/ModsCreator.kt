package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherManifestType
import net.treset.mc_version_loader.launcher.LauncherModsDetails
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.util.CreationStatus
import net.treset.treelauncher.backend.util.exception.ComponentCreationException
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class ModsCreator : GenericComponentCreator {
    private val types: List<String>?
    private val versions: List<String>?
    private var gameManifest: LauncherManifest? = null

    constructor(
        name: String?,
        typeConversion: Map<String, LauncherManifestType>,
        componentsManifest: LauncherManifest,
        types: List<String>?,
        versions: List<String>?,
        gameManifest: LauncherManifest?
    ) : super(
        LauncherManifestType.MODS_COMPONENT,
        null,
        null,
        name,
        typeConversion,
        appConfig().modsDefaultIncludedFiles,
        appConfig().modsDefaultDetails,
        componentsManifest
    ) {
        this.types = types
        this.versions = versions
        this.gameManifest = gameManifest
        this.defaultStatus = CreationStatus(CreationStatus.DownloadStep.MODS, null)
    }

    constructor(
        name: String?,
        inheritsFrom: Pair<LauncherManifest?, LauncherModsDetails?>,
        componentsManifest: LauncherManifest,
        gameManifest: LauncherManifest?
    ) : super(
        LauncherManifestType.MODS_COMPONENT,
        null,
        inheritsFrom.first,
        name,
        null,
        null,
        null,
        componentsManifest
    ) {
        types = null
        versions = null
        this.gameManifest = gameManifest
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.MODS, null)
    }

    constructor(uses: Pair<LauncherManifest?, LauncherModsDetails?>) : super(
        LauncherManifestType.MODS_COMPONENT,
        uses.first,
        null,
        null,
        null,
        null,
        null,
        null
    ) {
        types = null
        versions = null
        defaultStatus = (CreationStatus(CreationStatus.DownloadStep.MODS, null))
    }

    @Throws(ComponentCreationException::class)
    override fun createComponent(): String {
        val result: String = super.createComponent()
        newManifest?.let {
            val details = LauncherModsDetails(types, versions, listOf())
            try {
                LauncherFile.of(it.directory, appConfig().modsDefaultDetails).write(details)
            } catch (e: IOException) {
                attemptCleanup()
                throw ComponentCreationException("Failed to create mods component: failed to write details", e)
            }
            return result
        }
        LOGGER.error { "Failed to create mods component: invalid data" }
        attemptCleanup()
        throw ComponentCreationException("Failed to create mods component: invalid data")
    }

    override val parentManifestFileName: String
        get() = gameManifest?.components?.get(0)?: super.parentManifestFileName

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
