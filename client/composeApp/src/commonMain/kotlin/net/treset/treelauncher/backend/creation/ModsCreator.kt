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
    private val modsType: String?
    private val modsVersion: String?
    private var gameManifest: LauncherManifest? = null

    constructor(
        name: String?,
        typeConversion: Map<String, LauncherManifestType>,
        componentsManifest: LauncherManifest,
        modsType: String?,
        modsVersion: String?,
        gameManifest: LauncherManifest?
    ) : super(
        LauncherManifestType.MODS_COMPONENT,
        null,
        null,
        name,
        typeConversion,
        appConfig().MODS_DEFAULT_INCLUDED_FILES,
        appConfig().MODS_DEFAULT_DETAILS,
        componentsManifest
    ) {
        this.modsType = modsType
        this.modsVersion = modsVersion
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
        modsType = null
        modsVersion = null
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
        modsType = null
        modsVersion = null
        defaultStatus = (CreationStatus(CreationStatus.DownloadStep.MODS, null))
    }

    @Throws(ComponentCreationException::class)
    override fun createComponent(): String {
        val result: String = super.createComponent()
        newManifest?.let {
            val details = LauncherModsDetails(modsType, modsVersion, listOf())
            try {
                LauncherFile.of(it.directory, appConfig().MODS_DEFAULT_DETAILS).write(details)
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
