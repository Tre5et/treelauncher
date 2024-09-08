package net.treset.treelauncher.components.mods.display

import androidx.compose.ui.graphics.painter.Painter
import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mcdl.exception.FileDownloadException
import net.treset.mcdl.mods.*
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.data.LauncherMod
import net.treset.treelauncher.backend.mods.ModDownloader
import net.treset.treelauncher.backend.util.ModProviderStatus
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.loadNetworkImage
import net.treset.treelauncher.components.mods.ModContext
import java.io.IOException
import java.time.LocalDateTime

class LauncherModDisplay(
    val mod: LauncherMod,
    private val modContext: ModContext
): ModDisplay() {
    var downloading = false
        private set(value) {
            field = value
            recomposeData()
        }
    var image: Painter? = null
        private set(value) {
            field = value
            recomposeData()
        }
    var enabled = mod.isEnabled
        private set(value) {
            field = value
            recomposeData()
        }
    var selectLatest = false
        set(value) {
            field = value
            recomposeData()
        }
    var modrinthStatus = ModProviderStatus.UNAVAILABLE
        private set(value) {
            field = value
            recomposeData()
        }


    var curseforgeStatus = ModProviderStatus.UNAVAILABLE
        private set(value) {
            field = value
            recomposeData()
        }

    var versions: List<ModVersionData>? = null
        private set(value) {
            field = value
            recomposeData()
        }
    var currentVersion: ModVersionData =
        object: GenericModVersion() {
            override fun getDatePublished(): LocalDateTime? = null
            override fun getDownloads(): Int = 0
            override fun getName(): String? = null
            override fun getVersionNumber(): String = mod.version
            override fun getDownloadUrl(): String? = null
            override fun getModLoaders(): MutableList<String> = mutableListOf()
            override fun getGameVersions(): MutableList<String> = mutableListOf()
            override fun updateRequiredDependencies(): MutableList<ModVersionData> = mutableListOf()
            override fun getParentMod(): ModData? = null
            override fun setParentMod(p0: ModData?) {}
            override fun getModProviders(): MutableList<ModProvider> = mutableListOf()
            override fun getModVersionType(): ModVersionType? = null
        }
        private set(value) {
            field = value
            recomposeData()
        }
    var modData: ModData? = null
        private set(value) {
            field = value
            recomposeData()
        }

    init {
        updateModProviders()
        loadImage()
        loadVersions()
    }

    fun checkForUpdates() {
        versions?.let {
            if (it.isNotEmpty()) {
                if (appSettings().isModsUpdate) {
                    if (currentVersion.versionNumber != it.first().versionNumber) {
                        downloadVersion(it.first())
                    }
                    if(appSettings().isModsEnable && !enabled) {
                        changeEnabled()
                    }
                }
            } else if(appSettings().isModsDisable && enabled) {
                changeEnabled()
            }
        }
        selectLatest = true
    }

    fun loadImage() {
        Thread {
            mod.iconUrl?.let { url ->
                try {
                    loadNetworkImage(url)?.let {
                        image = it
                    }
                } catch(ignored: IOException) {}
            }
        }.start()
    }

    fun loadVersions() {
        versions ?: Thread {
            if(modData == null) {
                try {
                    modData = mod.modData
                } catch (e: FileDownloadException) {
                    LOGGER.debug(e) { "Failed to get mod data for ${mod.fileName}, this may be correct" }
                    versions = emptyList()
                }
            }
            modData?.let {
                it.setVersionConstraints(modContext.versions, modContext.types.map { it.id }, modContext.providers)
                versions = try {
                    it.getVersions()
                        .sortedWith { a, b -> a.datePublished.compareTo(b.datePublished) * -1 }
                } catch (e: FileDownloadException) {
                    AppContext.error(e)
                    emptyList()
                }.also { vs ->
                    vs.firstOrNull {
                        it.versionNumber == currentVersion.versionNumber
                    }?.let {
                        currentVersion = it
                    }
                }
            }
        }.start()
    }

    fun downloadVersion(version: ModVersionData) {
        downloading = true
        version.downloadProviders = modContext.providers
        modContext.registerChangingJob { currentMods ->
            LOGGER.debug { "Downloading mod ${mod.fileName} version ${version.versionNumber}" }

            try {
                ModDownloader(
                    mod,
                    modContext.directory,
                    modContext.types,
                    modContext.versions,
                    currentMods,
                    modContext.providers,
                    false //modContext.enableOnDownload
                ).download(
                    version
                )
            } catch (e: Exception) {
                AppContext.error(e)
                return@registerChangingJob
            }

            currentVersion = version

            downloading = false

            updateModProviders()
        }
    }

    fun changeEnabled() {
        modContext.registerChangingJob {
            LOGGER.debug { "Changing mod state of ${mod.fileName} to ${!enabled}" }

            val modFile: LauncherFile = LauncherFile.of(
                modContext.directory,
                "${mod.fileName}${if (enabled) "" else ".disabled"}"
            )
            val newFile: LauncherFile = LauncherFile.of(
                modContext.directory,
                "${mod.fileName}${if (enabled) ".disabled" else ""}"
            )
            if(!modFile.exists() && newFile.exists()) {
                LOGGER.debug { "Mod is already in correct state, not changing" }
                mod.isEnabled = !enabled
                enabled = !enabled
                return@registerChangingJob
            }

            LOGGER.debug { "Renaming mod file ${modFile.path} -> ${newFile.path}" }

            try {
                modFile.moveTo(newFile)
            } catch(e: IOException) {
                AppContext.error(IOException("Failed to move mod file", e))
            }

            mod.isEnabled = !enabled
            enabled = !enabled

            LOGGER.debug { "Mod state changed" }
        }
    }

    fun deleteMod() {
        modContext.registerChangingJob { mods ->
            val oldFile: LauncherFile = LauncherFile.of(
                modContext.directory,
                "${mod.fileName}${if (enabled) "" else ".disabled"}"
            )
            LOGGER.debug { "Deleting mod file: ${oldFile.path}" }
            try {
                oldFile.remove()
            } catch(e: IOException) {
                AppContext.error(IOException("Failed to delete mod file", e))
                return@registerChangingJob
            }
            mods.remove(mod)
            LOGGER.debug { "Mod file deleted" }
        }
    }

    fun updateModProviders() {
        modrinthStatus = if(mod.currentProvider == "modrinth") {
            ModProviderStatus.CURRENT
        } else if(mod.downloads.any { it.provider == "modrinth" }) {
            ModProviderStatus.AVAILABLE
        } else {
            ModProviderStatus.UNAVAILABLE
        }

        curseforgeStatus = if(mod.currentProvider == "curseforge") {
            ModProviderStatus.CURRENT
        } else if(mod.downloads.any { it.provider == "curseforge" }) {
            ModProviderStatus.AVAILABLE
        } else {
            ModProviderStatus.UNAVAILABLE
        }
    }

    override fun recomposeData(): ModDisplayData {
        return ModDisplayData(
            mod = this.mod,
            downloading = this.downloading,
            image = this.image,
            enabled = this.enabled,
            selectLatest = this.selectLatest,
            modrinthStatus = this.modrinthStatus,
            curseforgeStatus = this.curseforgeStatus,
            versions = this.versions,
            currentVersion = this.currentVersion,
            modData = this.modData,
            startDownload = this::downloadVersion,
            changeEnabled = this::changeEnabled,
            deleteMod = this::deleteMod,
        ).also(onRecomposeData)
    }
}

private val LOGGER = KotlinLogging.logger {  }