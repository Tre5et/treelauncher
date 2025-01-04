package dev.treset.treelauncher.backend.data.manifest

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.Painter
import com.google.gson.annotations.SerializedName
import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.mods.*
import dev.treset.mcdl.mods.curseforge.CurseforgeMod
import dev.treset.mcdl.mods.modrinth.ModrinthMod
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.LauncherModDownload
import dev.treset.treelauncher.backend.data.getEnabled
import dev.treset.treelauncher.backend.mods.ModDownloader
import dev.treset.treelauncher.backend.mods.modVersionFromString
import dev.treset.treelauncher.backend.util.ModProviderStatus
import dev.treset.treelauncher.backend.util.extractNameVersionFromFile
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.loadNetworkImage
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import dev.treset.treelauncher.backend.util.serialization.MutableDataStateList
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.IOException
import java.nio.file.StandardCopyOption

@Serializable
class LauncherMod(
    val currentProvider: MutableDataState<String?> = mutableStateOf(null),
    val description: MutableDataState<String?> = mutableStateOf(null),
    @SerializedName("enabled", alternate = ["is_enabled"])
    val enabled: MutableDataState<Boolean>,
    val url: MutableDataState<String?> = mutableStateOf(null),
    val iconUrl: MutableDataState<String?> = mutableStateOf(null),
    val name: MutableDataState<String> = mutableStateOf(""),
    val version: MutableDataState<String?> = mutableStateOf(null),
    val downloads: MutableDataStateList<LauncherModDownload>,
    @Transient override val file: MutableState<LauncherFile> = mutableStateOf(LauncherFile.of(""))
): Manifest() {
    constructor(
        currentProvider: String?,
        description: String?,
        enabled: Boolean,
        url: String?,
        iconUrl: String?,
        name: String?,
        version: String?,
        downloads: List<LauncherModDownload>,
        file: LauncherFile = LauncherFile.of()
    ): this(
        mutableStateOf(currentProvider),
        mutableStateOf(description),
        mutableStateOf(enabled),
        mutableStateOf(url),
        mutableStateOf(iconUrl),
        mutableStateOf(name ?: ""),
        mutableStateOf(version),
        downloads.toMutableStateList(),
        mutableStateOf(file)
    )

    init {
        if(version.value == null) {
            val v = try {
                modFile?.name?.extractNameVersionFromFile(".jar", ".jar.disabled")?.second
            } catch (e: IllegalArgumentException) {
                null
            }
            v?.let { version.value = it }
                ?: LOGGER.debug { "Not able to extract version from mod file: ${modFile?.name}" }
        }
        if(name.value.isBlank()) {
            val n = try {
                modFile?.name?.extractNameVersionFromFile(".jar", ".jar.disabled")?.first
            } catch (e: IllegalArgumentException) {
                null
            }
            n?.let { name.value = it }
                ?: LOGGER.debug { "Not able to extract name from mod file: ${modFile?.name}" }
        }
    }

    override val type = LauncherManifestType.LAUNCHER_MOD
    @Transient override var expectedType = LauncherManifestType.LAUNCHER_MOD

    @Transient val fileName = derivedStateOf { file.value.nameWithoutExtension }

    @Transient var modData: ModData? = null

    @Transient val downloading = mutableStateOf(false)
    @Transient val visible = mutableStateOf(false)
    @Transient val image: MutableState<Painter?> = mutableStateOf(null)

    @Transient val updateAvailable: MutableState<Boolean?> = mutableStateOf(null)

    @Transient val selectLatest = mutableStateOf(0)
    @Transient val modrinthStatus = mutableStateOf(ModProviderStatus.UNAVAILABLE)

    @Transient val curseforgeStatus = mutableStateOf(ModProviderStatus.UNAVAILABLE)
    @Transient var versions: MutableState<List<ModVersionData>?> = mutableStateOf(null)

    @Transient val currentVersion = mutableStateOf(
        version.value?.let {
            modVersionFromString(it)
        }
    )

    @Transient val hasMetaData = derivedStateOf { version.value != null }

    fun setVersion(version: String?) {
        if(version == null) {
            return
        }
        this.version.value = version
        currentVersion.value = modVersionFromString(version)
    }

    fun initializeDisplay(component: ModsComponent) {
        loadImage()
        loadVersions(component)
        updateModProviders()
    }

    fun updateEnabled() {
        if(getModFile(true)?.exists() == true) {
            enabled.value = true
        } else if(getModFile(false)?.exists() == true) {
            enabled.value = false
        } else {
            AppContext.error(IllegalStateException("No enabled or disabled mod file found for ${fileName.value}"))
        }
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
                    LOGGER.debug(e) { "Failed to get mod data for ${fileName.value}, this may be correct" }
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
                        it.versionNumber == currentVersion.value?.versionNumber
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
                        if (currentVersion.value?.versionNumber != it.first().versionNumber) {
                            downloadVersion(it.first(), component)
                        }
                        if (component.enableOnUpdate.value && !enabled.value) {
                            changeEnabled(component)
                        }
                    } else {
                        updateAvailable.value = currentVersion.value?.versionNumber != it.first().versionNumber
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
            LOGGER.debug { "Downloading mod ${fileName.value} version ${version.versionNumber}" }

            try {
                ModDownloader(
                    this,
                    component.modsDirectory,
                    component.types.toVersionTypes(),
                    component.versions,
                    currentMods,
                    component.providers.getEnabled(),
                    component.enableOnUpdate.value
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

            val newFile = getModFile(!enabled.value)
            if(modFile?.exists() != true && newFile?.exists() == true) {
                LOGGER.warn { "Mod is already in correct state, not changing" }
                enabled.value = !enabled.value
                return@registerJob
            }
            if(modFile?.exists() != true) {
                AppContext.error(IOException("Can't change mod state, mod file not found"))
            }

            LOGGER.debug { "Renaming mod file ${modFile?.path} -> ${newFile?.path}" }

            try {
                newFile?.let { modFile?.moveTo(it) }
            } catch(e: IOException) {
                AppContext.error(IOException("Failed to move mod file", e))
            }

            updateEnabled()

            LOGGER.debug { "Mod state changed" }
        }
    }

    fun delete(component: ModsComponent) {
        component.registerJob { mods ->
            val oldFile = modFile
            LOGGER.debug { "Deleting mod file: ${oldFile?.path}" }
            try {
                file.value.delete()
            } catch(e: IOException) {
                AppContext.error(IOException("Failed to delete mod data file"))
            }
            try {
                oldFile?.remove()
            } catch(e: IOException) {
                AppContext.error(IOException("Failed to delete mod file, still removed mod", e))
            }
            mods.remove(this)
            LOGGER.debug { "Mod file deleted" }
        }
    }

    @Throws(IOException::class)
    fun setModFile(file: LauncherFile) {
        if(file == modFile) {
            LOGGER.debug { "Mod file is unchanged" }
        }

        val oldFile = this.file.value

        this.file.value = file.renamed("${file.nameWithoutExtension}.json")
        updateEnabled()

        write()

        if(oldFile.exists()) {
            oldFile.delete()
        }
    }

    @Throws(IOException::class)
    fun changeAssociatedMod(modFile: LauncherFile, directory: LauncherFile) {
        changeAssociatedMod(nameFromModFile(modFile), directory)
    }

    @Throws(IOException::class)
    fun changeAssociatedMod(modName: String, directory: LauncherFile) {
        if(modName == this.fileName.value) {
            LOGGER.debug { "Mod name is unchanged: $modName" }
            return
        }

        val oldFile = file.value

        this.file.value = directory.child("$modName.json")
        updateEnabled()

        LOGGER.debug { "Writing to new meta file: ${file.value.absolutePath}" }
        write()

        if(oldFile.isChildOf(directory) && oldFile.exists()) {
            LOGGER.debug { "Deleting old meta file: ${oldFile.absolutePath}" }
            oldFile.remove()
        }
    }

    @Throws(IOException::class)
    fun setImportingMod(file: LauncherFile, directory: LauncherFile, preserveEnabledState: Boolean = true) {
        if(file == modFile) {
            LOGGER.debug { "Mod file is unchanged: ${fileName.value}" }
            return
        }


        val backupFile = try {
            modFile.let {
                if (it?.isChildOf(directory) == true) {
                    LOGGER.debug { "Backing up old mod file: ${it.name}" }
                    it.renamed("${it.name}.bak").also { bak ->
                        it.moveTo(bak, StandardCopyOption.REPLACE_EXISTING)
                    }
                } else null
            }
        } catch (e: IOException) {
            LOGGER.warn { "Unable to create backup file: ${modFile?.name}" }
            null
        }

        var componentFile = file
        try {
            if (!file.isChildOf(directory)) {
                LOGGER.debug { "Moving mod file to component directory: ${file.name}" }
                componentFile = directory.child(file.name)

                file.copyTo(componentFile, StandardCopyOption.REPLACE_EXISTING)
            }

            if (preserveEnabledState) {
                val stateFile = fileAsState(file, enabled.value)
                if (stateFile != componentFile) {
                    componentFile.copyTo(stateFile, StandardCopyOption.REPLACE_EXISTING)
                    componentFile = stateFile
                }
            }

            changeAssociatedMod(nameFromModFile(componentFile), directory)
        } catch (e: IOException) {
            LOGGER.warn { "Failed mod import, restoring" }
            try {
                componentFile.remove()
                modFile?.let {
                    backupFile?.moveTo(it, StandardCopyOption.REPLACE_EXISTING)
                }
            } catch (e1: IOException) {
                LOGGER.warn { "Failed to restore old mod file" }
            }
            throw e
        }

        backupFile?.let {
            LOGGER.debug { "Removing old backup file: ${backupFile.name}" }
            try {
                it.remove()
            } catch (e: IOException) {
                LOGGER.warn { "Unable to remove backup file: ${backupFile.name}" }
            }
        }
    }

    fun getModFile(enabled: Boolean? = null): LauncherFile? {
        val actualEnabled = enabled ?: this.enabled.value

        return rawDirectory?.child("${file.value.nameWithoutExtension}${if(actualEnabled) ".jar" else ".jar.disabled"}")
    }

    @Throws(IOException::class)
    override fun write() {
        if(hasMetaData.value) {
            super.write()
        }
    }

    val modFile get() = getModFile()

    companion object {
        fun rawFile(file: LauncherFile, directory: LauncherFile, checkEnabled: Boolean = true) = LauncherMod(
            currentProvider = null,
            description = null,
            enabled = true,
            url = null,
            iconUrl = null,
            name = null,
            version = null,
            downloads = emptyList(),
            file = directory.child("${nameFromModFile(file)}.json")
        ).apply {
            if(checkEnabled) {
                updateEnabled()
            }
        }

        @Throws(IOException::class)
        fun importing(file: LauncherFile, directory: LauncherFile) = LauncherMod(
            currentProvider = null,
            description = null,
            enabled = true,
            url = null,
            iconUrl = null,
            name = null,
            version = null,
            downloads = listOf(),
            file = LauncherFile.of()
        ).apply {
            setImportingMod(file, directory, false)
        }

        fun loadOrRawFile(file: LauncherFile, directory: LauncherFile): LauncherMod {
            val metaFile = directory.child("${nameFromModFile(file)}.json")

            if(metaFile.isFile) {
                try {
                    return readFile(metaFile)
                } catch (e: IOException) {
                    LOGGER.warn { "Unable to read meta file: ${metaFile.absolutePath}" }
                }
            }


            return rawFile(file, directory)
         }

        fun nameFromModFile(file: LauncherFile): String {
            if(file.name.endsWith(".disabled")) {
                return file.nameWithoutExtension.substring(0, file.nameWithoutExtension.lastIndexOf('.'))
            }
            return file.nameWithoutExtension
        }

        fun fileAsState(file: LauncherFile, enabled: Boolean): LauncherFile {
            if(enabled != file.name.endsWith(".disabled")) {
                return file
            }

            if(enabled) {
                return file.renamed(file.nameWithoutExtension)
            }
            return file.renamed("${file.name}.disabled")
        }

        private val LOGGER = KotlinLogging.logger {  }
    }
}
