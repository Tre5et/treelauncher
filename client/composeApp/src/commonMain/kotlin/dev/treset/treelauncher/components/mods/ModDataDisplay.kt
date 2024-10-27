package dev.treset.treelauncher.components.mods

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.painter.Painter
import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.mods.ModData
import dev.treset.mcdl.mods.ModProvider
import dev.treset.mcdl.mods.ModVersionData
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.mods.ModDownloader
import dev.treset.treelauncher.backend.util.ModProviderStatus
import dev.treset.treelauncher.backend.util.isSame
import dev.treset.treelauncher.backend.util.loadNetworkImage
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException

class ModDataDisplay(
    val mod: ModData,
    ctx: ModDisplayContext
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
        loadVersions(ctx)
        updateLauncherMod(ctx)
    }

    fun download(version: ModVersionData, ctx: ModDisplayContext) {
        downloading.value = true
        ctx.registerJob { currentMods ->
            LOGGER.debug { "Downloading mod ${mod.name} version ${version.versionNumber}" }
            try {
                launcherMod.value = ModDownloader(
                    launcherMod.value,
                    ctx.directory,
                    ctx.types,
                    ctx.versions,
                    currentMods,
                    ctx.providers,
                    AppSettings.isModsEnable.value
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

    private fun updateLauncherMod(ctx: ModDisplayContext) {
        ctx.registerJob { mods ->
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

    private fun loadVersions(ctx: ModDisplayContext) {
        Thread {
            try {
                mod.setVersionConstraints(ctx.versions, ctx.types.map { it.id }, ctx.providers)
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