package net.treset.treelauncher.backend.mods

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mcdl.exception.FileDownloadException
import net.treset.mcdl.mods.ModData
import net.treset.mcdl.mods.ModProvider
import net.treset.mcdl.mods.ModVersionData
import net.treset.treelauncher.backend.data.LauncherMod
import net.treset.treelauncher.backend.data.toLauncherMod
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.isSame
import net.treset.treelauncher.generic.VersionType
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
            val fileName = "${it.fileName}${if (it.isEnabled) "" else ".disabled"}"

            val oldFile: LauncherFile = LauncherFile.of(
                directory,
                fileName
            )

            if(oldFile.isFile) {
                val newFile = LauncherFile.of(
                    directory,
                    "${fileName}.old"
                )

                LOGGER.debug { "Renaming old mod file: ${oldFile.name} -> ${newFile.name}" }

                oldFile.moveTo(newFile, StandardCopyOption.REPLACE_EXISTING)
            } else {
                LOGGER.warn { "Mod is specified but old file does not exist: ${oldFile.name}" }
            }

        }

        val newMod = try {
            downloadRequired(
                versionData,
                launcherMod?.isEnabled != false || enableOnDownload,
                !existing.contains(launcherMod)
            )
        } catch(e: FileDownloadException) {
            launcherMod?.let {
                val fileName = "${it.fileName}${if (it.isEnabled) "" else ".disabled"}"

                val newFile = LauncherFile.of(
                    directory,
                    "${fileName}.old"
                )

                if(newFile.isFile) {
                    val oldFile: LauncherFile = LauncherFile.of(
                        directory,
                        fileName
                    )

                    LOGGER.debug { "Restoring old mod file: ${newFile.name} -> ${oldFile.name}" }

                    newFile.moveTo(oldFile, StandardCopyOption.REPLACE_EXISTING)
                } else {
                    LOGGER.warn { "Mod is specified but file to restore does not exist: ${newFile.name}" }
                }
            }
            throw e
        }

        launcherMod?.let {
            val oldFileName = "${it.fileName}${if (it.isEnabled) "" else ".disabled"}.old"

            it.version = newMod.version
            it.isEnabled = newMod.isEnabled
            it.downloads = newMod.downloads
            it.fileName = newMod.fileName
            it.name = newMod.name
            it.downloads = newMod.downloads
            it.iconUrl = newMod.iconUrl
            it.currentProvider = newMod.currentProvider
            it.description = newMod.description
            it.url = newMod.url

            val newFile = LauncherFile.of(
                directory,
                oldFileName
            )

            if (newFile.isFile) {
                LOGGER.debug { "Deleting old mod file: ${newFile.name}" }
                newFile.remove()
            } else {
                LOGGER.warn { "Mod is specified but backup file does not exist: ${newFile.name}" }
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
        val newMod = versionData.download(directory).toLauncherMod()
        if(!enabled) {
            LOGGER.debug { "Disabling new mod file: ${newMod.fileName}" }
            val file = LauncherFile.of(directory, newMod.fileName)
            val disabledFile = LauncherFile.of(directory, "${newMod.fileName}.disabled")
            try {
                file.moveTo(disabledFile, StandardCopyOption.REPLACE_EXISTING)
            } catch(e: IOException) {
                throw FileDownloadException("Failed to disable new mod file: ${file.name}", e)
            }
        }
        newMod.isEnabled = enabled

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