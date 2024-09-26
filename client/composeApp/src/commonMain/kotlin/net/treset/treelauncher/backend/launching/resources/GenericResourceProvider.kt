package net.treset.treelauncher.backend.launching.resources

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.manifest.Component
import net.treset.treelauncher.backend.util.exception.GameResourceException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import java.io.IOException
import java.nio.file.StandardCopyOption

open class GenericResourceProvider<T: Component>(
    component: T,
    gameDataDir: LauncherFile
): ComponentResourceProvider<T>(component, gameDataDir) {
    @Throws(GameResourceException::class)
    override fun includeResources() {
        val dir: LauncherFile = LauncherFile.of(component.directory)
        val files = dir.listFiles()
        LOGGER.debug { "Adding included files: ${component.type}, id=${component.id}, includedFiles=$files" }
        val exceptionQueue: MutableList<Exception> = ArrayList<Exception>()
        for (f in files) {
            if(f.name == appConfig().manifestFileName || f.name == appConfig().includedFilesBackupDir) {
                LOGGER.debug { "Skipping manifest file: ${f.name}" }
                continue
            }
            LOGGER.debug { "Moving file: ${f.name}" }
            if (f.isFile() || f.isDirectory()) {
                try {
                    f.atomicMoveTo(LauncherFile.of(gameDataDir, f.getName()), StandardCopyOption.REPLACE_EXISTING)
                } catch (e: Exception) {
                    exceptionQueue.add(e)
                    LOGGER.warn(e) { "Unable to move included files: unable to copy file: manifestId=${component.id}" }
                }
            } else {
                exceptionQueue.add(IOException("Included files directory contains invalid file type: manifestId=${component.id}"))
            }
        }
        if (exceptionQueue.isNotEmpty()) {
            throw GameResourceException("Unable to move included files: unable to copy ${exceptionQueue.size} files", exceptionQueue[0])
        }
        LOGGER.debug { "Added included files: manifestId=${component.id}}" }
    }

    var includedFiles = component.includedFiles.map { p -> PatternString(p, true).changeDirectoryEnding() }.toTypedArray()

    @Throws(GameResourceException::class)
    override fun removeResources(files: MutableList<LauncherFile>, unexpected: Boolean) {
        LOGGER.debug { "Removing included files: ${component.type.name.lowercase()}, id=${component.id}, includedFiles=${component.includedFiles}, files=$files" }

        includedFiles = component.includedFiles.map { p -> PatternString(p, true).changeDirectoryEnding() }.toTypedArray()
        val toRemove: MutableList<LauncherFile> = mutableListOf()
        val exceptionQueue: MutableList<IOException> = mutableListOf()
        for (f in files) {
            if(shouldRemove(f)) {
                LOGGER.debug { "Moving file: ${f.name}" }
                try {
                    f.atomicMoveTo(LauncherFile.of(component.directory, f.getName()), StandardCopyOption.REPLACE_EXISTING)
                    toRemove.add(f)
                } catch (e: IOException) {
                    exceptionQueue.add(e)
                    LOGGER.warn { "Unable to remove included files: unable to move file: component_type=${component.type.name.lowercase()}, component=${component.id}, file=${f}" }
                }
            }
        }
        files.removeAll(toRemove)
        if (exceptionQueue.isNotEmpty()) {
            throw GameResourceException("Unable to remove included files: unable to move ${exceptionQueue.size} files: component_type=${component.type.name.lowercase()}, component=${component.id}", exceptionQueue[0])
        }
        LOGGER.debug { "Removed included files: manifestId=${component.id}}" }
    }

    open fun shouldRemove(file: LauncherFile): Boolean {
        return PatternString.matchesAny(file.getLauncherName(), includedFiles)
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}