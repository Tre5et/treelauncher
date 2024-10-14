package dev.treset.treelauncher.components.mods.display

import androidx.compose.ui.graphics.painter.Painter
import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.mods.ModData
import dev.treset.mcdl.mods.ModProvider
import dev.treset.mcdl.mods.ModVersionData
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.mods.ModDownloader
import dev.treset.treelauncher.backend.util.ModProviderStatus
import dev.treset.treelauncher.backend.util.isSame
import dev.treset.treelauncher.backend.util.loadNetworkImage
import dev.treset.treelauncher.components.mods.SearchContext
import java.io.IOException

class ModDataSearchDisplay(
    val mod: ModData,
    private val searchContext: SearchContext
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
    var launcherMod: LauncherMod? = null
    var modrinthStatus = if(mod.modProviders.contains(ModProvider.MODRINTH)) {
        ModProviderStatus.AVAILABLE
    } else {
        ModProviderStatus.UNAVAILABLE
    }
        private set(value) {
            field = value
            recomposeData()
        }
    var curseforgeStatus = if(mod.modProviders.contains(ModProvider.CURSEFORGE)) {
        ModProviderStatus.AVAILABLE
    } else {
        ModProviderStatus.UNAVAILABLE
    }
        private set(value) {
            field = value
            recomposeData()
        }

    var versions: List<ModVersionData>? = null
        private set(value) {
            field = value
            recomposeData()
        }
    var currentVersion: ModVersionData? = null
        private set(value) {
            field = value
            recomposeData()
        }

    var visible = false
        private set(value) {
            if(field != value) {
                field = value
                onVisibility(value)
            }
        }

    init {
        registerRecheck()
        loadImage()
        loadVersions()
        updateLauncherMod()
    }

    fun startDownload(version: ModVersionData) {
        downloading = true
        searchContext.registerChangingJob { currentMods ->
            LOGGER.debug { "Downloading mod ${mod.name} version ${version.versionNumber}" }
            try {
                ModDownloader(
                    launcherMod,
                    searchContext.directory,
                    searchContext.types,
                    searchContext.versions,
                    currentMods,
                    searchContext.providers,
                    searchContext.enableOnDownload
                ).download(
                    version
                )

                currentVersion = version
            } catch(e: Exception) {
                AppContext.error(e)
            }

            downloading = false

            updateModProviders()
            searchContext.recheck()
        }
    }

    private fun registerRecheck() {
        searchContext.registerRecheck {
            updateLauncherMod()
        }
    }

    private fun updateLauncherMod() {
        searchContext.registerChangingJob { mods ->
            mods.firstOrNull {
                it.isSame(mod)
            }.let {
                if(it != launcherMod) {
                    launcherMod = it

                    updateModProviders()
                    updateCurrentVersion()
                }
            }
        }
    }

    private fun updateCurrentVersion() {
        launcherMod?.let { lm ->
            versions?.firstOrNull {
                it.versionNumber == lm.version
            }?.let {
                currentVersion = it
            }
        }
    }

    private fun updateModProviders() {
        (launcherMod?.let {
            if(it.currentProvider == "modrinth") {
                ModProviderStatus.CURRENT
            } else null
        } ?: run {
            if(mod.modProviders.contains(ModProvider.MODRINTH)) {
                ModProviderStatus.AVAILABLE
            } else {
                ModProviderStatus.UNAVAILABLE
            }
        }).let {
            if(it != modrinthStatus) {
                modrinthStatus = it
            }
        }

        (launcherMod?.let {
            if(it.currentProvider == "curseforge") {
                ModProviderStatus.CURRENT
            } else null
        } ?: run {
            if(mod.modProviders.contains(ModProvider.CURSEFORGE)) {
                ModProviderStatus.AVAILABLE
            } else {
                ModProviderStatus.UNAVAILABLE
            }
        }).let {
            if(it != modrinthStatus) {
                curseforgeStatus = it
            }
        }
    }

    private fun loadImage() {
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

    private fun loadVersions() {
        Thread {
            try {
                mod.setVersionConstraints(searchContext.versions, searchContext.types.map { it.id }, searchContext.providers)
                versions = mod.versions
                    .sortedWith { a, b -> a.datePublished.compareTo(b.datePublished) * -1 }
            } catch (e: FileDownloadException) {
                AppContext.error(e)
            }
            updateCurrentVersion()
        }.start()
    }

    override fun recomposeData(): ModDisplayData {
        return ModDisplayData(
            mod = launcherMod,
            downloading = downloading,
            image = image,
            enabled = true,
            selectLatest = false,
            versions = versions,
            currentVersion = currentVersion,
            modrinthStatus = modrinthStatus,
            curseforgeStatus = curseforgeStatus,
            modData = mod,
            startDownload = ::startDownload,
            changeEnabled = {},
            deleteMod = {},
            setVisible = { visible = it }
        ).also(onRecomposeData)
    }
}

private val LOGGER = KotlinLogging.logger {  }