package net.treset.treelauncher.backend.launching

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.data.manifest.ComponentManifest
import net.treset.treelauncher.backend.util.exception.GameResourceException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import java.io.IOException
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.util.*

class ResourceManager(private var instanceData: InstanceData) {

    @Throws(GameResourceException::class)
    fun prepareResources() {
        try {
            instanceData.setActive(true)
        } catch (e: IOException) {
            throw GameResourceException("Failed to prepare resources: unable to set instance active", e)
        }
        addIncludedFiles(
            listOf(
                instanceData.instance.first,
                instanceData.optionsComponent,
                instanceData.resourcepacksComponent,
                instanceData.savesComponent
            )
        )
        instanceData.modsComponent?.let { addIncludedFiles(listOf(it.first)) }
        renameComponents()
        LOGGER.info {"Prepared resources for launch, instance=${instanceData.instance.first.id}"}
    }

    @Throws(IOException::class)
    fun setLastPlayedTime() {
        LOGGER.debug { "Setting last played time: instance=${instanceData.instance.first.id}" }
        val time = LocalDateTime.now()
        instanceData.instance.first.lastUsedTime = time
        instanceData.savesComponent.lastUsedTime = time
        instanceData.resourcepacksComponent.lastUsedTime = time
        instanceData.optionsComponent.lastUsedTime = time
        instanceData.modsComponent?.first?.lastUsedTime = time
        LauncherFile.of(
            instanceData.instance.first.directory,
            appConfig().manifestFileName
        ).write(instanceData.instance.first)
        LauncherFile.of(
            instanceData.savesComponent.directory,
            appConfig().manifestFileName
        ).write(instanceData.savesComponent)
        LauncherFile.of(
            instanceData.resourcepacksComponent.directory,
            appConfig().manifestFileName
        ).write(instanceData.resourcepacksComponent)
        LauncherFile.of(
            instanceData.optionsComponent.directory,
            appConfig().manifestFileName
        ).write(instanceData.optionsComponent)
        instanceData.modsComponent?.let {
            LauncherFile.of(
                it.first.directory,
                appConfig().manifestFileName
            ).write(it.first)
        }
        LOGGER.debug { "Set last played time: instance=${instanceData.instance.first.id}" }
    }

    @Throws(IOException::class)
    fun addPlayDuration(duration: Long) {
        LOGGER.debug { "Adding play duration: instance=${instanceData.instance.first.id}, duration=$duration" }

        val oldTime: Long = instanceData.instance.second.totalTime
        instanceData.instance.second.totalTime = oldTime + duration
        LauncherFile.of(
            instanceData.instance.first.directory,
            instanceData.instance.first.details
        ).write(instanceData.instance.second)
        LOGGER.debug { "Added play duration: instance=${instanceData.instance.first.id}, duration=$duration, totalTime=${instanceData.instance.second.totalTime}" }
    }

    @Throws(GameResourceException::class)
    fun cleanupGameFiles(mergeFiles: Boolean = false) {
        try {
            LOGGER.debug { "Cleaning up game files: instance=${instanceData.instance.first.id}" }
            try {
                undoRenameComponents()
                val gameDataFilesList = gameDataFiles
                removeExcludedFiles(gameDataFilesList)
                removeIncludedFiles(arrayOf(instanceData.savesComponent), gameDataFilesList, mergeFiles)
                instanceData.modsComponent?.let { removeIncludedFiles(arrayOf(it.first), gameDataFilesList, mergeFiles) }
                removeIncludedFiles(arrayOf(instanceData.optionsComponent, instanceData.resourcepacksComponent), gameDataFilesList, mergeFiles)
                removeIncludedFiles(instanceData.instance.first, gameDataFilesList, mergeFiles, true)
            } catch (e: GameResourceException) {
                throw GameResourceException("Unable to cleanup game files", e)
            }
            try {
                instanceData.setActive(false)
            } catch (e: IOException) {
                throw GameResourceException("Unable to cleanup game files: unable to set instance inactive", e)
            }
        } catch (e: Exception) {
            throw GameResourceException("Unable to cleanup game files", e)
        }
        LOGGER.info { "Game files cleaned up" }
    }

    @Throws(GameResourceException::class)
    private fun renameComponents() {
        LOGGER.debug { "Renaming components: instance=${instanceData.instance.first.id}" }
        try {
            val savesLocation = LauncherFile.of(instanceData.gameDataDir, "saves")
            LauncherFile.of(instanceData.savesComponent.directory).moveTo(savesLocation)
            instanceData.savesComponent.directory = savesLocation.path
        } catch (e: IOException) {
            throw GameResourceException("Unable to rename saves file", e)
        }
        instanceData.savesComponent.directory = LauncherFile.of(instanceData.gameDataDir, "saves").path
        instanceData.modsComponent?.let { modsComponents ->
            try {
                val modsLocation = LauncherFile.of(instanceData.gameDataDir, "mods")
                LauncherFile.of(modsComponents.first.directory).moveTo(modsLocation)
                modsComponents.first.directory = modsLocation.path
            } catch (e: IOException) {
                throw GameResourceException("Unable to rename mods file", e)
            }
            modsComponents.first.directory = LauncherFile.of(instanceData.gameDataDir, "mods").path
        }
    }

    @Throws(GameResourceException::class)
    private fun addIncludedFiles(components: List<ComponentManifest>) {
        LOGGER.debug { "Adding included files: instance=${instanceData.instance.first.id}" }
        val exceptionQueue: MutableList<GameResourceException> = mutableListOf()
        for (c in components) {
            try {
                addIncludedFiles(c)
            } catch (e: GameResourceException) {
                exceptionQueue.add(e)
                LOGGER.warn(e) { "Unable to get included files: manifestId=${c.id}"}
            }
        }
        if (exceptionQueue.isNotEmpty()) {
            throw GameResourceException("Unable to get included files for ${exceptionQueue.size} components", exceptionQueue[0])
        }
        LOGGER.debug { "Added included files: instance=${instanceData.instance.first.id}" }
    }

    @Throws(GameResourceException::class)
    private fun addIncludedFiles(manifest: ComponentManifest) {
        val includedFilesDir: LauncherFile = LauncherFile.of(manifest.directory, appConfig().includedFilesDirName)
        if (!includedFilesDir.isDirectory()) {
            try {
                includedFilesDir.createDir()
            } catch (e: IOException) {
                throw GameResourceException("Unable to get included files: unable to create included files directory: manifestId=${manifest.id}", e)
            }
        }
        val files = includedFilesDir.listFiles()
        LOGGER.debug { "Adding included files: ${manifest.type}, id=${manifest.id}, includedFiles=$files" }
        val exceptionQueue: MutableList<Exception> = ArrayList<Exception>()
        for (f in files) {
            LOGGER.debug { "Moving file: ${f.name}" }
            if (f.isFile() || f.isDirectory()) {
                try {
                    LauncherFile.of(f).copyTo(
                        LauncherFile.of(instanceData.gameDataDir, f.getName()),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                } catch (e: Exception) {
                    exceptionQueue.add(e)
                    LOGGER.warn(e) { "Unable to move included files: unable to copy file: manifestId=${manifest.id}" }
                }
            } else {
                exceptionQueue.add(IOException("Included files directory contains invalid file type: manifestId=${manifest.id}"))
            }
        }
        if (exceptionQueue.isNotEmpty()) {
            throw GameResourceException("Unable to move included files: unable to copy ${exceptionQueue.size} files", exceptionQueue[0])
        }
        LOGGER.debug { "Added included files: manifestId=${manifest.id}}" }
    }

    @Throws(GameResourceException::class)
    private fun undoRenameComponents() {
        LOGGER.debug { "Undoing component renames: instance=${instanceData.instance.first.id}" }
        val newSavesDir = LauncherFile.ofData(instanceData.launcherDetails.savesDir, "${instanceData.savesPrefix}_${instanceData.savesComponent.id}")
        try {
            LauncherFile.of(instanceData.savesComponent.directory).moveTo(newSavesDir)
            instanceData.savesComponent.directory = newSavesDir.path
        } catch (e: IOException) {
            throw GameResourceException("Unable to cleanup launch resources: rename saves file failed", e)
        }
        instanceData.savesComponent.directory = newSavesDir.path
        instanceData.modsComponent?.let { modsComponents ->
            val newModsDir = LauncherFile.ofData(instanceData.launcherDetails.modsDir, "${instanceData.modsPrefix}_${modsComponents.first.id}")
            try {
                LauncherFile.of(modsComponents.first.directory).moveTo(newModsDir)
                instanceData.modsComponent?.first?.directory = newModsDir.path
            } catch (e: IOException) {
                throw GameResourceException("Unable to cleanup launch resources: rename mods file failed", e)
            }
            modsComponents.first.directory = newModsDir.path
        } ?: run {
            LOGGER.debug { "No mods component to cleanup, deleting mods dir" }
            val modsDir = LauncherFile.of(instanceData.gameDataDir, "mods")
            if (modsDir.exists()) {
                try {
                    modsDir.remove()
                } catch (e: IOException) {
                    throw GameResourceException("Unable to cleanup launch resources: unable to delete mods directory", e)
                }
            }
        }
        LOGGER.debug { "Undid component renames: instance=${instanceData.instance.first.id}" }
    }

    @get:Throws(GameResourceException::class)
    private val gameDataFiles: MutableList<LauncherFile>
        get() {
            LOGGER.debug { "Getting game data files: instance=${instanceData.instance.first.id}" }
            val gameDataDir: LauncherFile = instanceData.gameDataDir
            if (!gameDataDir.isDirectory()) {
                throw GameResourceException("Unable to cleanup launch resources: game data directory not found")
            }
            val gameDataFiles = gameDataDir.listFiles()
            LOGGER.debug { "Got game data files: instance=${instanceData.instance.first.id}" }
            return gameDataFiles.toMutableList()
        }

    private fun removeExcludedFiles(files: MutableList<LauncherFile>) {
        LOGGER.debug { "Removing excluded files: instance=${instanceData.instance.first.id}, files=$files" }
        val toRemove: MutableList<LauncherFile> = mutableListOf()
        for (f in files) {
            val launcherName = f.getLauncherName()

            if (launcherName == appConfig().manifestFileName || PatternString.matchesAny(launcherName, instanceData.gameDataExcludedFiles)) {
                LOGGER.debug { "Removing excluded file: ${f.name}" }
                toRemove.add(f)
            }
        }
        files.removeAll(toRemove.toSet())
        LOGGER.debug { "Removed excluded files: instance=${instanceData.instance.first.id}" }
    }

    @Throws(GameResourceException::class)
    private fun removeIncludedFiles(components: Array<ComponentManifest>, files: MutableList<LauncherFile>, mergeFiles: Boolean) {
        LOGGER.debug { "Removing included files: instance=${instanceData.instance.first.id}, files=$files" }
        val exceptionQueue: MutableList<GameResourceException> = mutableListOf()
        for (component in components) {
            if (component.includedFiles.isEmpty()) {
                LOGGER.debug { "No included files: ${component.type.name.lowercase()}, manifestId=${component.id}" }
                continue
            }
            try {
                removeIncludedFiles(component, files, mergeFiles)
            } catch (e: GameResourceException) {
                exceptionQueue.add(e)
                LOGGER.warn(e) { "Unable to remove included files: ${component.type.name.lowercase()}, manifestId=${component.id}" }
            }
        }
        if (exceptionQueue.isNotEmpty()) {
            throw GameResourceException("Unable to remove included files for ${exceptionQueue.size} components", exceptionQueue[0])
        }
        LOGGER.debug { "Removed included files: instance=${instanceData.instance.first.id}}" }
    }

    @Throws(GameResourceException::class)
    private fun removeIncludedFiles(component: ComponentManifest, files: MutableList<LauncherFile>, mergeFiles: Boolean, allFiles: Boolean = false) {
        LOGGER.debug { "Removing included files: ${component.type.name.lowercase()}, id=${component.id}, includedFiles=${component.includedFiles}, files=$files" }
        val includedFilesDir: LauncherFile =
            LauncherFile.of(component.directory, appConfig().includedFilesDirName)
        val oldIncludedFilesDir: LauncherFile =
            LauncherFile.of(component.directory, "${appConfig().includedFilesDirName}_old")
        if (includedFilesDir.exists()) {
            try {
                if (oldIncludedFilesDir.exists()) {
                    oldIncludedFilesDir.remove()
                }
                if(mergeFiles) {
                    includedFilesDir.copyTo(oldIncludedFilesDir, StandardCopyOption.REPLACE_EXISTING)
                } else {
                    includedFilesDir.moveTo(oldIncludedFilesDir, StandardCopyOption.REPLACE_EXISTING)
                }
            } catch (e: IOException) {
                throw GameResourceException("Unable to remove included files: unable to move included files directory: component_type=${component.type.name.lowercase()}, component=${component.id}", e)
            }
        }
        try {
            includedFilesDir.createDir()
        } catch (e: IOException) {
            throw GameResourceException("Unable to remove included files: unable to create included files directory: component_type=${component.type.name.lowercase()}, component=${component.id}" )
        }

        val currentFiles = component.includedFiles.map { p -> PatternString(p, true).changeDirectoryEnding() }.toTypedArray()

        val toRemove: MutableList<LauncherFile> = mutableListOf()
        val exceptionQueue: MutableList<IOException> = mutableListOf()
        for (f in files) {
            if(allFiles || PatternString.matchesAny(f.getLauncherName(), currentFiles)) {
                toRemove.add(f)

                LOGGER.debug { "Moving file: ${f.name}" }
                try {
                    f.moveTo(LauncherFile.of(component.directory, appConfig().includedFilesDirName, f.getName()), StandardCopyOption.REPLACE_EXISTING)
                } catch (e: IOException) {
                    exceptionQueue.add(e)
                    LOGGER.warn { "Unable to remove included files: unable to move file: component_type=${component.type.name.lowercase()}, component=${component.id}, file=${f}" }
                }
            }
        }
        files.removeAll(toRemove.toSet())
        if (exceptionQueue.isNotEmpty()) {
            throw GameResourceException("Unable to remove included files: unable to move ${exceptionQueue.size} files: component_type=${component.type.name.lowercase()}, component=${component.id}", exceptionQueue[0])
        }
        LOGGER.debug { "Removed included files: manifestId=${component.id}}" }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
