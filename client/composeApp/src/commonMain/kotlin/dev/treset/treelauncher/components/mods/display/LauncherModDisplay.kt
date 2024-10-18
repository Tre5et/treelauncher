package dev.treset.treelauncher.components.mods.display

import androidx.compose.ui.graphics.painter.Painter
import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.mods.*
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.mods.ModDownloader
import dev.treset.treelauncher.backend.util.ModProviderStatus
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.loadNetworkImage
import dev.treset.treelauncher.components.mods.ModContext
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
            onDownloading(value)
        }
    var image: Painter? = null
        private set(value) {
            field = value
            recomposeData()
        }
    var enabled = mod.enabled
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

    var visible = false
        private set(value) {
            if(field != value) {
                field = value
                onVisibility(value)
                recomposeData()
            }
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

    fun checkForUpdates(): Boolean {
        versions?.let {
            if (it.isNotEmpty()) {
                if (AppSettings.isModsUpdate.value) {
                    if (currentVersion.versionNumber != it.first().versionNumber) {
                        downloadVersion(it.first())
                    }
                    if(AppSettings.isModsEnable.value && !enabled) {
                        changeEnabled()
                    }
                }
            } else if(AppSettings.isModsDisable.value && enabled) {
                changeEnabled()
            }
        }
        selectLatest = true
        return versions?.getOrNull(0)?.versionNumber?.let { it != currentVersion.versionNumber } ?: false
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
                mod.enabled = !enabled
                enabled = !enabled
                return@registerChangingJob
            }

            LOGGER.debug { "Renaming mod file ${modFile.path} -> ${newFile.path}" }

            try {
                modFile.moveTo(newFile)
            } catch(e: IOException) {
                AppContext.error(IOException("Failed to move mod file", e))
            }

            mod.enabled = !enabled
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
            setVisible = { visible = it }
        ).also(onRecomposeData)
    }
}

private val LOGGER = KotlinLogging.logger {  }