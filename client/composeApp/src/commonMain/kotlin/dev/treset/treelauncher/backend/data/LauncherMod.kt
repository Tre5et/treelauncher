package dev.treset.treelauncher.backend.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.graphics.painter.Painter
import com.google.gson.annotations.SerializedName
import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.mods.*
import dev.treset.mcdl.mods.curseforge.CurseforgeMod
import dev.treset.mcdl.mods.modrinth.ModrinthMod
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.manifest.ModsComponent
import dev.treset.treelauncher.backend.data.manifest.toVersionTypes
import dev.treset.treelauncher.backend.mods.ModDownloader
import dev.treset.treelauncher.backend.util.ModProviderStatus
import dev.treset.treelauncher.backend.util.loadNetworkImage
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import dev.treset.treelauncher.backend.util.serialization.MutableDataStateList
import dev.treset.treelauncher.components.mods.getEnabled
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.IOException
import java.time.LocalDateTime

@Serializable
class LauncherMod(
    val currentProvider: MutableDataState<String?> = mutableStateOf(null),
    val description: MutableDataState<String?> = mutableStateOf(null),
    @SerializedName("enabled", alternate = ["is_enabled"])
    val enabled: MutableDataState<Boolean>,
    val url: MutableDataState<String?> = mutableStateOf(null),
    val iconUrl: MutableDataState<String?> = mutableStateOf(null),
    val name: MutableDataState<String>,
    val fileName: MutableDataState<String>,
    val version: MutableDataState<String>,
    val downloads: MutableDataStateList<LauncherModDownload>
) {

    constructor(
        currentProvider: String?,
        description: String?,
        enabled: Boolean,
        url: String?,
        iconUrl: String?,
        name: String,
        fileName: String,
        version: String,
        downloads: List<LauncherModDownload>
    ): this(
        mutableStateOf(currentProvider),
        mutableStateOf(description),
        mutableStateOf(enabled),
        mutableStateOf(url),
        mutableStateOf(iconUrl),
        mutableStateOf(name),
        mutableStateOf(fileName),
        mutableStateOf(version),
        downloads.toMutableStateList()
    )

    @Transient var modData: ModData? = null

    @Transient val downloading = mutableStateOf(false)
    @Transient val visible = mutableStateOf(false)
    @Transient val image: MutableState<Painter?> = mutableStateOf(null)

    @Transient val updateAvailable: MutableState<Boolean?> = mutableStateOf(null)

    @Transient val selectLatest = mutableStateOf(0)
    @Transient val modrinthStatus = mutableStateOf(ModProviderStatus.UNAVAILABLE)

    @Transient val curseforgeStatus = mutableStateOf(ModProviderStatus.UNAVAILABLE)
    @Transient var versions: MutableState<List<ModVersionData>?> = mutableStateOf(null)

    @Transient val currentVersion = mutableStateOf<ModVersionData>(
        object: GenericModVersion() {
            override fun getDatePublished(): LocalDateTime? = null
            override fun getDownloads(): Int = 0
            override fun getName(): String? = null
            override fun getVersionNumber(): String = version.value
            override fun getDownloadUrl(): String? = null
            override fun getModLoaders(): MutableList<String> = mutableListOf()
            override fun getGameVersions(): MutableList<String> = mutableListOf()
            override fun updateRequiredDependencies(): MutableList<ModVersionData> = mutableListOf()
            override fun getParentMod(): ModData? = null
            override fun setParentMod(p0: ModData?) {}
            override fun getModProviders(): MutableList<ModProvider> = mutableListOf()
            override fun getModVersionType(): ModVersionType? = null
        }
    )

    fun initializeDisplay(component: ModsComponent) {
        loadImage()
        loadVersions(component)
        updateModProviders()
    }

    @Throws(FileDownloadException::class)
    fun loadModData(): ModData {
        if (ModsDL.getModrinthUserAgent() == null || ModsDL.getCurseforgeApiKey().isBlank()) {
            throw FileDownloadException("Modrinth user agent or curseforge api key not set")
        }
        val mods = ArrayList<ModData>()
        for (download in downloads) {
            if (download.provider == "modrinth") {
                val modrinthMod = ModrinthMod.get(download.id)
                if (modrinthMod.name != null && modrinthMod.name.isNotBlank()) {
                    mods.add(modrinthMod)
                }
            }
            if (download.provider == "curseforge") {
                val curseforgeMod = CurseforgeMod.get(download.id.toLong())
                if (curseforgeMod.name != null && curseforgeMod.name.isNotBlank()) {
                    mods.add(curseforgeMod)
                }
            }
        }
        if (mods.isEmpty()) {
            throw FileDownloadException("No mod data found: mod=${name.value}")
        }
        if (mods.size == 1) {
            return mods[0].also {
                modData = it
            }
        }
        return CombinedModData(mods[0], mods[1]).also {
            modData = it
        }
    }

    fun loadImage() {
        Thread {
            iconUrl.value?.let { url ->
                try {
                    loadNetworkImage(url)?.let {
                        image.value = it
                    }
                } catch(ignored: IOException) {}
            }
        }.start()
    }

    fun loadVersions(component: ModsComponent) {
        Thread {
            modData ?: try {
                    loadModData()
                } catch (e: FileDownloadException) {
                    LOGGER.debug(e) { "Failed to get mod data for ${fileName}, this may be correct" }
                    versions.value = listOf()
                }
            modData?.let {
                it.setVersionConstraints(component.versions, component.types, component.providers.getEnabled())
                versions.value = try {
                    it.versions.sortedWith { a, b -> a.datePublished.compareTo(b.datePublished) * -1 }
                } catch (e: FileDownloadException) {
                    AppContext.error(e)
                    emptyList()
                }.also { vs ->
                    vs.firstOrNull {
                        it.versionNumber == currentVersion.value.versionNumber
                    }?.let {
                        currentVersion.value = it
                    }
                }
            }
        }.start()
    }

    fun updateModProviders() {
        modrinthStatus.value = if(currentProvider.value == "modrinth") {
            ModProviderStatus.CURRENT
        } else if(downloads.any { it.provider == "modrinth" }) {
            ModProviderStatus.AVAILABLE
        } else {
            ModProviderStatus.UNAVAILABLE
        }

        curseforgeStatus.value = if(currentProvider.value == "curseforge") {
            ModProviderStatus.CURRENT
        } else if(downloads.any { it.provider == "curseforge" }) {
            ModProviderStatus.AVAILABLE
        } else {
            ModProviderStatus.UNAVAILABLE
        }
    }

    fun checkForUpdates(component: ModsComponent) {
        Thread {
            updateAvailable.value = null
            versions.value?.let {
                if (it.isNotEmpty()) {
                    if (component.autoUpdate.value) {
                        if (currentVersion.value.versionNumber != it.first().versionNumber) {
                            downloadVersion(it.first(), component)
                        }
                        if (component.enableOnUpdate.value && !enabled.value) {
                            changeEnabled(component)
                        }
                    } else {
                        updateAvailable.value = currentVersion.value.versionNumber != it.first().versionNumber
                    }
                } else {
                    updateAvailable.value = false
                    if (component.disableOnNoVersion.value && enabled.value) {
                        changeEnabled(component)
                    }
                }
            } ?: run { updateAvailable.value = false }
            selectLatest.value++
        }.start()
    }

    fun downloadVersion(version: ModVersionData, component: ModsComponent) {
        downloading.value = true
        updateAvailable.value = null
        version.downloadProviders = component.providers.getEnabled()
        component.registerJob { currentMods ->
            LOGGER.debug { "Downloading mod ${fileName} version ${version.versionNumber}" }

            try {
                ModDownloader(
                    this,
                    component.modsDirectory,
                    component.types.toVersionTypes(),
                    component.versions,
                    currentMods,
                    component.providers.getEnabled(),
                    false //modContext.enableOnDownload
                ).download(
                    version
                )
            } catch (e: Exception) {
                AppContext.error(e)
                return@registerJob
            }

            currentVersion.value = version

            downloading.value = false

            updateModProviders()
        }
    }

    fun changeEnabled(component: ModsComponent) {
        component.registerJob {
            LOGGER.debug { "Changing mod state of ${fileName.value} to ${!enabled.value}" }

            val modFile = component.modsDirectory.child(
                "${fileName.value}${if (enabled.value) "" else ".disabled"}"
            )
            val newFile = component.modsDirectory.child(
                "${fileName.value}${if (enabled.value) ".disabled" else ""}"
            )
            if(!modFile.exists() && newFile.exists()) {
                LOGGER.warn { "Mod is already in correct state, not changing" }
                enabled.value = !enabled.value
                return@registerJob
            }
            if(!modFile.exists()) {
                LOGGER.warn { "Can't change mod state, mod file not found" }
                AppContext.error(IOException("Can't change mod state, mod file not found"))
            }

            LOGGER.debug { "Renaming mod file ${modFile.path} -> ${newFile.path}" }

            try {
                modFile.moveTo(newFile)
            } catch(e: IOException) {
                AppContext.error(IOException("Failed to move mod file", e))
            }

            enabled.value = !enabled.value

            LOGGER.debug { "Mod state changed" }
        }
    }

    fun delete(component: ModsComponent) {
        component.registerJob { mods ->
            val oldFile = component.modsDirectory.child(
                "${fileName}${if (enabled.value) "" else ".disabled"}"
            )
            LOGGER.debug { "Deleting mod file: ${oldFile.path}" }
            try {
                oldFile.remove()
            } catch(e: IOException) {
                AppContext.error(IOException("Failed to delete mod file", e))
                return@registerJob
            }
            mods.remove(this)
            LOGGER.debug { "Mod file deleted" }
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {  }
    }
}
