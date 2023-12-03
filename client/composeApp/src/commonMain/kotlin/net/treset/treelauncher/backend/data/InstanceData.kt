package net.treset.treelauncher.backend.data

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.launcher.*
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import java.io.IOException

class InstanceData(
    var launcherDetails: LauncherDetails,
    var launcherDetailsFile: LauncherFile,
    var instance: Pair<LauncherManifest, LauncherInstanceDetails>,
    var versionComponents: Array<Pair<LauncherManifest, LauncherVersionDetails>>,
    var javaComponent: LauncherManifest,
    var optionsComponent: LauncherManifest,
    var resourcepacksComponent: LauncherManifest,
    var savesComponent: LauncherManifest,
    var modsComponent: Pair<LauncherManifest, LauncherModsDetails>?,
    var gameDataDir: LauncherFile,
    var assetsDir: LauncherFile,
    var librariesDir: LauncherFile,
    var modsPrefix: String,
    var savesPrefix: String,
    var gameDataExcludedFiles: Array<PatternString>
) {
    @Throws(IOException::class)
    fun setActive(active: Boolean) {
        launcherDetails.activeInstance = if (active) instance.first.id else null
        try {
            launcherDetailsFile.write(launcherDetails)
        } catch (e: IOException) {
            throw IOException("Unable to set instance active: unable to write launcher details", e)
        }
    }

    @Throws(IOException::class)
    fun delete(files: LauncherFiles) {
        files.instanceManifest.components?.let {comp ->
            if (!comp.remove(instance.first.id)) {
                throw IOException("Unable to delete instance: unable to remove instance from launcher manifest")
            }
            try {
                LauncherFile.of(files.instanceManifest.directory, appConfig().MANIFEST_FILE_NAME).write(files.instanceManifest)
            } catch (e: IOException) {
                throw IOException("Unable to delete instance: unable to write launcher manifest", e)
            }
        }
        try {
            LauncherFile.of(instance.first.directory).remove()
        } catch (e: IOException) {
            throw IOException("Unable to delete instance: unable to delete instance directory", e)
        }
        LOGGER.debug { "Instance deleted: id=${instance.first.id}" }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger{}

        @Throws(FileLoadException::class)
        fun of(instance: Pair<LauncherManifest, LauncherInstanceDetails>, files: LauncherFiles): InstanceData {
            try {
                files.reloadAll()
            } catch (e: FileLoadException) {
                throw FileLoadException("Failed to load instance data: file reload failed", e)
            }
            val versionComponents: MutableList<Pair<LauncherManifest, LauncherVersionDetails>> = mutableListOf()

            var firstComponent: Pair<LauncherManifest, LauncherVersionDetails>? = null
            for (v in files.versionComponents) {
                if (v.first.id == instance.second.versionComponent) {
                    firstComponent = v
                    break
                }
            }
            firstComponent?: throw FileLoadException("Failed to load instance data: unable to find version component: versionId=${instance.second.versionComponent}")
            var currentComponent: Pair<LauncherManifest, LauncherVersionDetails> = firstComponent
            versionComponents.add(currentComponent)
            while (currentComponent.second.depends != null && currentComponent.second.depends.isNotBlank()
            ) {
                var found = false
                for (v in files.versionComponents) {
                    if (v.first.id == currentComponent.second.depends) {
                        currentComponent = v
                        found = true
                        break
                    }
                }
                if (!found) {
                    throw FileLoadException("Failed to load instance data: unable to find dependent version component: versionId=${currentComponent.second.depends}")
                }
                versionComponents.add(currentComponent)
            }

            var javaComponent: LauncherManifest? = null
                for (v in versionComponents) {
                    if (v.second.java != null && v.second.java.isNotBlank()) {
                        for (j in files.javaComponents) {
                            if (j.id == v.second.java) {
                                javaComponent = j
                                break
                            }
                        }
                        break
                    }
                }
                javaComponent ?: throw FileLoadException("Failed to load instance data: unable to find suitable java component")

            var optionsComponent: LauncherManifest? = null
            for (o in files.optionsComponents) {
                if (o.id == instance.second.optionsComponent) {
                    optionsComponent = o
                    break
                }
            }
            optionsComponent ?: throw FileLoadException("Failed to load instance data: unable to find options component: optionsId=" + instance.second.optionsComponent)

            var resourcepacksComponent: LauncherManifest? = null
            for (r in files.resourcepackComponents) {
                if (r.id == instance.second.resourcepacksComponent) {
                    resourcepacksComponent = r
                    break
                }
            }
            resourcepacksComponent ?: throw FileLoadException("Failed to load instance data: unable to find resourcepacks component: resourcepacksId=" + instance.second.resourcepacksComponent)

            var savesComponent: LauncherManifest? = null
            for (s in files.savesComponents) {
                if (s.id == instance.second.savesComponent) {
                    savesComponent = s
                    break
                }
            }
            savesComponent ?: throw FileLoadException("Failed to load instance data: unable to find saves component: savesId=" + instance.second.savesComponent)

            var modsComponent: Pair<LauncherManifest, LauncherModsDetails>? = null
            if (instance.second.modsComponent != null && instance.second.modsComponent.isNotBlank()) {
                for (m in files.modsComponents) {
                    if (m.first.id == instance.second.modsComponent) {
                        modsComponent = m
                        break
                    }
                }
                modsComponent ?: throw FileLoadException("Failed to load instance data: unable to find mods component: modsId=" + instance.second.modsComponent)
            }

            val gameDataExcludedFiles: ArrayList<PatternString> = ArrayList()
            for (c in files.gameDetailsManifest.components) {
                gameDataExcludedFiles.add(PatternString(c))
            }
            gameDataExcludedFiles.add(PatternString(files.modsManifest.prefix + ".*"))
            gameDataExcludedFiles.add(PatternString(files.savesManifest.prefix + ".*"))

            return InstanceData(
                files.launcherDetails,
                LauncherFile.of(
                    files.mainManifest.directory,
                    files.mainManifest.details
                ),
                instance,
                versionComponents.toTypedArray(),
                javaComponent,
                optionsComponent,
                resourcepacksComponent,
                savesComponent,
                modsComponent,
                LauncherFile.ofData(files.launcherDetails.gamedataDir),
                LauncherFile.ofData(files.launcherDetails.assetsDir),
                LauncherFile.ofData(files.launcherDetails.librariesDir),
                files.modsManifest.prefix,
                files.savesManifest.prefix,
                gameDataExcludedFiles.toTypedArray()
            )
        }
    }
}
