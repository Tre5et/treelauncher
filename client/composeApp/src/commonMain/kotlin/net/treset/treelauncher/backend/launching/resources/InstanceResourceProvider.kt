package net.treset.treelauncher.backend.launching.resources

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.treelauncher.backend.data.manifest.InstanceComponent
import net.treset.treelauncher.backend.util.exception.GameResourceException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import java.io.IOException

class InstanceResourceProvider(
    component: InstanceComponent,
    gameDataDir: LauncherFile
): GenericResourceProvider<InstanceComponent>(component, gameDataDir) {
    @Throws(GameResourceException::class)
    override fun removeResources(files: MutableList<LauncherFile>, unexpected: Boolean) {
        if(!unexpected) {
            removeExcluded(files)
        }
        super.removeResources(files, unexpected)
    }

    override fun shouldRemove(file: LauncherFile): Boolean {
        return true
    }

    @Throws(GameResourceException::class)
    fun removeExcluded(files: MutableList<LauncherFile>) {
        LOGGER.debug { "Removing excluded files of instance, id=${component.id}, files=$files" }

        val excludedFiles = component.ignoredFiles.map { p -> PatternString(p, true).changeDirectoryEnding() }.toTypedArray()
        val toRemove: MutableList<LauncherFile> = mutableListOf()
        val exceptionQueue: MutableList<IOException> = mutableListOf()
        for(f in files) {
            if(PatternString.matchesAny(f.getLauncherName(), excludedFiles)) {
                LOGGER.debug { "Moving file: ${f.name}" }
                try {
                    toRemove.add(f)
                    f.delete()
                } catch(e: IOException) {
                    exceptionQueue.add(e)
                    LOGGER.warn(e) { "Unable to move excluded files: unable to copy file: manifestId=${component.id}" }
                }
            }
        }
        files.removeAll(toRemove)
        if(exceptionQueue.isNotEmpty()) {
            throw GameResourceException("Unable to move excluded files: unable to copy ${exceptionQueue.size} files", exceptionQueue[0])
        }
        LOGGER.debug { "Removed excluded files: manifestId=${component.id}" }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}