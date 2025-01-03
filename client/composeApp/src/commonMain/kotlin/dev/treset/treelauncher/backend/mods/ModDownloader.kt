package dev.treset.treelauncher.backend.mods

import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.mods.ModData
import dev.treset.mcdl.mods.ModProvider
import dev.treset.mcdl.mods.ModVersionData
import dev.treset.treelauncher.backend.data.manifest.LauncherMod
import dev.treset.treelauncher.backend.data.toLauncherMod
import dev.treset.treelauncher.backend.util.assignFrom
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.isSame
import dev.treset.treelauncher.generic.VersionType
import java.io.IOException
import java.nio.file.StandardCopyOption

class ModDownloader(
    val launcherMod: LauncherMod?,
    val directory: LauncherFile,
    val versionTypes: List<VersionType>,
    val versions: List<String>,
    val existing: MutableList<LauncherMod>,
    val modProviders: List<ModProvider>,
    val enableOnDownload: Boolean = false,
) {
    @Throws(FileDownloadException::class, IOException::class)
    fun download(
        versionData: ModVersionData
    ): LauncherMod {
        LOGGER.debug { "Downloading mod: name=${versionData.name}, version=${versionData.versionNumber}" }

        launcherMod?.let {
            it.modFile?.let { oldFile ->
                if (oldFile.isFile) {
                    val newFile = oldFile.renamed("${oldFile.name}.old")

                    LOGGER.debug { "Renaming old mod file: ${oldFile.name} -> ${newFile.name}" }

                    oldFile.moveTo(newFile, StandardCopyOption.REPLACE_EXISTING)
                } else {
                    LOGGER.warn { "Mod is specified but old file does not exist: ${oldFile.name}" }
                }
            }
        }

        val newMod = try {
            downloadRequired(
                versionData,
                launcherMod?.enabled?.value != false || enableOnDownload,
                !existing.contains(launcherMod)
            )
        } catch(e: FileDownloadException) {
            launcherMod?.let {
                it.modFile?.let { oldFile ->
                    val newFile = oldFile.renamed("${oldFile.name}.old")

                    if (newFile.isFile) {
                        LOGGER.debug { "Restoring old mod file: ${newFile.name} -> ${oldFile.name}" }

                        newFile.moveTo(oldFile, StandardCopyOption.REPLACE_EXISTING)
                    } else {
                        LOGGER.warn { "Mod is specified but file to restore does not exist: ${newFile.name}" }
                    }
                }
            }
            throw e
        }

        launcherMod?.let {
            val oldFile = it.modFile?.renamed("${it.modFile}.old")

            it.setVersion(newMod.version.value)
            it.enabled.value = newMod.enabled.value
            it.downloads.assignFrom(newMod.downloads)
            it.name.value = newMod.name.value
            it.downloads.assignFrom(newMod.downloads)
            it.iconUrl.value = newMod.iconUrl.value
            it.currentProvider.value = newMod.currentProvider.value
            it.description.value = newMod.description.value
            it.url.value = newMod.url.value

            it.setModFile(newMod.modFile!!)

            if (oldFile?.isFile == true) {
                LOGGER.debug { "Deleting old mod file: ${oldFile.name}" }
                oldFile.remove()
            } else {
                LOGGER.warn { "Mod is specified but backup file does not exist: ${oldFile?.name}" }
            }
        }

        return newMod
    }

    @Throws(FileDownloadException::class)
    private fun downloadRequired(
        versionData: ModVersionData,
        enabled: Boolean,
        addToList: Boolean = true
    ): LauncherMod {
        LOGGER.debug {
            "Downloading mod file: ${versionData.name}"
        }
        versionData.downloadProviders = modProviders
        if(!directory.isDirectory) {
            try {
                directory.createDir()
            } catch (e: IOException) {
                throw FileDownloadException("Unable to create mods directory", e)
            }
        }
        val newMod = versionData.download(directory).toLauncherMod()
        if(!enabled) {
            LOGGER.debug { "Disabling new mod file: ${newMod.jarName}" }
            val file = newMod.modFile
            val disabledFile = newMod.getModFile(false)
            try {
                disabledFile?.let {
                    file?.moveTo(disabledFile, StandardCopyOption.REPLACE_EXISTING)
                }
            } catch(e: IOException) {
                throw FileDownloadException("Failed to disable new mod file: ${file?.name}", e)
            }
        }
        newMod.enabled.value = enabled

        if(addToList) {
            existing.add(newMod)
        }

        versionData.setDependencyConstraints(versions, versionTypes.map { it.id }, modProviders)
        for (d in versionData.requiredDependencies) {
            if (d == null) {
                continue
            }
            if (d.parentMod != null && !modExists(d.parentMod)) {
                LOGGER.debug { "Downloading mod dependency file: ${d.name} for: ${versionData.name}" }
                downloadRequired(d, true)
            } else {
                LOGGER.debug { "Skipping mod dependency file: ${d.name}" }
            }
        }

        LOGGER.debug { "Downloaded mod file: ${versionData.name}" }
        return newMod
    }

    private fun modExists(mod: ModData): Boolean {
        return existing.any {
            it.isSame(mod)
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}