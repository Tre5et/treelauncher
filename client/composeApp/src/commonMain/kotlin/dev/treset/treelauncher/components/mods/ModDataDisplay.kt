package dev.treset.treelauncher.components.mods

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.painter.Painter
import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.mods.ModData
import dev.treset.mcdl.mods.ModProvider
import dev.treset.mcdl.mods.ModVersionData
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.data.getEnabled
import dev.treset.treelauncher.backend.data.manifest.ModsComponent
import dev.treset.treelauncher.backend.data.manifest.toVersionTypes
import dev.treset.treelauncher.backend.mods.ModDownloader
import dev.treset.treelauncher.backend.util.ModProviderStatus
import dev.treset.treelauncher.backend.util.isSame
import dev.treset.treelauncher.backend.util.loadNetworkImage
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException

class ModDataDisplay(
    val mod: ModData,
    component: ModsComponent
) {
    var downloading = mutableStateOf(false)

    var image: MutableState<Painter?> = mutableStateOf(null)

    var launcherMod: MutableState<LauncherMod?> = mutableStateOf(null)

    var modrinthStatus = mutableStateOf(
        if (mod.modProviders.contains(ModProvider.MODRINTH)) {
            ModProviderStatus.AVAILABLE
        } else {
            ModProviderStatus.UNAVAILABLE
        }
    )

    var curseforgeStatus = mutableStateOf(
        if (mod.modProviders.contains(ModProvider.CURSEFORGE)) {
            ModProviderStatus.AVAILABLE
        } else {
            ModProviderStatus.UNAVAILABLE
        }
    )

    var versions: MutableState<List<ModVersionData>?> = mutableStateOf(null)

    var currentVersion: MutableState<ModVersionData?> = mutableStateOf(null)

    init {
        loadImage()
        loadVersions(component)
        updateLauncherMod(component)
    }

    fun download(version: ModVersionData, component: ModsComponent) {
        downloading.value = true
        component.registerJob { currentMods ->
            LOGGER.debug { "Downloading mod ${mod.name} version ${version.versionNumber}" }
            try {
                launcherMod.value = ModDownloader(
                    launcherMod.value,
                    component.modsDirectory,
                    component.types.toVersionTypes(),
                    component.versions,
                    currentMods,
                    component.providers.getEnabled(),
                    component.enableOnUpdate.value
                ).download(
                    version
                )

                currentVersion.value = version
            } catch(e: Exception) {
                AppContext.error(e)
            }

            downloading.value = false

            updateModProviders()
        }
    }

    private fun updateLauncherMod(component: ModsComponent) {
        component.registerJob { mods ->
            mods.firstOrNull {
                it.isSame(mod)
            }.let {
                if(it != launcherMod.value) {
                    launcherMod.value = it

                    updateModProviders()
                    updateCurrentVersion()
                }
            }
        }
    }

    private fun updateCurrentVersion() {
        launcherMod.value?.let { lm ->
            versions.value?.firstOrNull {
                it.versionNumber == lm.version.value
            }?.let {
                currentVersion.value = it
            }
        }
    }

    private fun updateModProviders() {
        (launcherMod.value?.let {
            if(it.currentProvider.value == "modrinth") {
                ModProviderStatus.CURRENT
            } else null
        } ?: run {
            if(mod.modProviders.contains(ModProvider.MODRINTH)) {
                ModProviderStatus.AVAILABLE
            } else {
                ModProviderStatus.UNAVAILABLE
            }
        }).let {
            if(it != modrinthStatus.value) {
                modrinthStatus.value = it
            }
        }

        (launcherMod.value?.let {
            if(it.currentProvider.value == "curseforge") {
                ModProviderStatus.CURRENT
            } else null
        } ?: run {
            if(mod.modProviders.contains(ModProvider.CURSEFORGE)) {
                ModProviderStatus.AVAILABLE
            } else {
                ModProviderStatus.UNAVAILABLE
            }
        }).let {
            if(it != modrinthStatus.value) {
                curseforgeStatus.value = it
            }
        }
    }

    private fun loadImage() {
        Thread {
            mod.iconUrl?.let { url ->
                try {
                    loadNetworkImage(url)?.let {
                        image.value = it
                    }
                } catch(ignored: IOException) {}
            }
        }.start()
    }

    private fun loadVersions(component: ModsComponent) {
        Thread {
            try {
                mod.setVersionConstraints(component.versions, component.types, component.providers.getEnabled())
                versions.value = mod.versions
                    .sortedWith { a, b -> a.datePublished.compareTo(b.datePublished) * -1 }
            } catch (e: FileDownloadException) {
                AppContext.error(e)
            }
            updateCurrentVersion()
        }.start()
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {  }
    }
}