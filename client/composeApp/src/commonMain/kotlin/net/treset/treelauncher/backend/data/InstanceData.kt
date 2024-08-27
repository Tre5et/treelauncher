package net.treset.treelauncher.backend.data

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.manifest.ComponentManifest
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import java.io.IOException

class InstanceData(
    var launcherDetails: LauncherDetails,
    var launcherDetailsFile: LauncherFile,
    var instance: Pair<ComponentManifest, LauncherInstanceDetails>,
    var versionComponents: Array<Pair<ComponentManifest, LauncherVersionDetails>>,
    var javaComponent: ComponentManifest,
    var optionsComponent: ComponentManifest,
    var resourcepacksComponent: ComponentManifest,
    var savesComponent: ComponentManifest,
    var modsComponent: Pair<ComponentManifest, LauncherModsDetails>?,
    var gameDataDir: LauncherFile,
    var assetsDir: LauncherFile,
    var librariesDir: LauncherFile,
    var modsPrefix: String,
    var savesPrefix: String,
    var gameDataExcludedFiles: Array<PatternString>
) {
    @Throws(FileLoadException::class)
    fun reloadVersionComponent(files: LauncherFiles) {
        this.versionComponents = getVersionComponents(instance, files)
    }

    @Throws(FileLoadException::class)
    fun reloadJavaComponent(files: LauncherFiles) {
        this.javaComponent = getJavaComponent(versionComponents, files)
    }

    @Throws(FileLoadException::class)
    fun reloadOptionsComponent(files: LauncherFiles) {
        this.optionsComponent = getOptionsComponent(instance, files)
    }

    @Throws(FileLoadException::class)
    fun reloadResourcepacksComponent(files: LauncherFiles) {
        this.resourcepacksComponent = getResourcepacksComponent(instance, files)
    }

    @Throws(FileLoadException::class)
    fun reloadSavesComponent(files: LauncherFiles) {
        this.savesComponent = getSavesComponent(instance, files)
    }

    @Throws(FileLoadException::class)
    fun reloadModsComponent(files: LauncherFiles) {
        this.modsComponent = getModsComponent(instance, files)
    }

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
        files.instanceManifest.components.let {comp ->
            if (!comp.remove(instance.first.id)) {
                throw IOException("Unable to delete instance: unable to remove instance from launcher manifest")
            }
            try {
                LauncherFile.of(files.instanceManifest.directory, appConfig().manifestFileName).write(files.instanceManifest)
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
        fun of(instance: Pair<ComponentManifest, LauncherInstanceDetails>, files: LauncherFiles): InstanceData {
            val versionComponents = getVersionComponents(instance, files)
            val virtualDir = versionComponents.firstOrNull{it.second.virtualAssets != null}?.second?.virtualAssets
            val assetsDir = virtualDir?.let {
                LauncherFile.ofData(files.launcherDetails.assetsDir, it)
            } ?: LauncherFile.ofData(files.launcherDetails.assetsDir)

            val gameDataExcludedFiles: ArrayList<PatternString> = ArrayList()
            gameDataExcludedFiles.add(PatternString(files.modsManifest.prefix + ".*"))
            gameDataExcludedFiles.add(PatternString(files.savesManifest.prefix + ".*"))

            return InstanceData(
                files.launcherDetails,
                LauncherFile.of(
                    files.mainManifest.directory,
                    files.mainManifest.details
                ),
                instance,
                versionComponents,
                getJavaComponent(versionComponents, files),
                getOptionsComponent(instance, files),
                getResourcepacksComponent(instance, files),
                getSavesComponent(instance, files),
                getModsComponent(instance, files),
                LauncherFile.ofData(files.launcherDetails.gamedataDir),
                assetsDir,
                LauncherFile.ofData(files.launcherDetails.librariesDir),
                files.modsManifest.prefix,
                files.savesManifest.prefix,
                gameDataExcludedFiles.toTypedArray()
            )
        }

        @Throws(FileLoadException::class)
        private fun getVersionComponents(instance: Pair<ComponentManifest, LauncherInstanceDetails>, files: LauncherFiles): Array<Pair<ComponentManifest, LauncherVersionDetails>> {
            val versionComponents: MutableList<Pair<ComponentManifest, LauncherVersionDetails>> = mutableListOf()

            var firstComponent: Pair<ComponentManifest, LauncherVersionDetails>? = null
            for (v in files.versionComponents) {
                if (v.first.id == instance.second.versionComponent) {
                    firstComponent = v
                    break
                }
            }
            firstComponent?: throw FileLoadException("Failed to load instance data: unable to find version component: versionId=${instance.second.versionComponent}")
            var currentComponent: Pair<ComponentManifest, LauncherVersionDetails> = firstComponent
            versionComponents.add(currentComponent)
            while (currentComponent.second.depends?.isNotBlank() == true) {
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

            return versionComponents.toTypedArray()
        }

        @Throws(FileLoadException::class)
        private fun getJavaComponent(versionComponents: Array<Pair<ComponentManifest, LauncherVersionDetails>>, files: LauncherFiles): ComponentManifest {
            var javaComponent: ComponentManifest? = null
            for (v in versionComponents) {
                if (v.second.java?.isNotBlank() == true) {
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
            return javaComponent
        }

        @Throws(FileLoadException::class)
        private fun getOptionsComponent(instance: Pair<ComponentManifest, LauncherInstanceDetails>, files: LauncherFiles): ComponentManifest {
            var optionsComponent: ComponentManifest? = null
            for (o in files.optionsComponents) {
                if (o.id == instance.second.optionsComponent) {
                    optionsComponent = o
                    break
                }
            }
            optionsComponent ?: throw FileLoadException("Failed to load instance data: unable to find options component: optionsId=" + instance.second.optionsComponent)
            return optionsComponent
        }

        @Throws(FileLoadException::class)
        private fun getResourcepacksComponent(instance: Pair<ComponentManifest, LauncherInstanceDetails>, files: LauncherFiles): ComponentManifest {
            var resourcepacksComponent: ComponentManifest? = null
            for (r in files.resourcepackComponents) {
                if (r.id == instance.second.resourcepacksComponent) {
                    resourcepacksComponent = r
                    break
                }
            }
            resourcepacksComponent ?: throw FileLoadException("Failed to load instance data: unable to find resourcepacks component: resourcepacksId=" + instance.second.resourcepacksComponent)
            return resourcepacksComponent
        }

        @Throws(FileLoadException::class)
        private fun getSavesComponent(instance: Pair<ComponentManifest, LauncherInstanceDetails>, files: LauncherFiles): ComponentManifest {
            for (s in files.savesComponents) {
                if (s.id == instance.second.savesComponent) {
                    return s
                }
            }
            throw FileLoadException("Failed to load instance data: unable to find saves component: savesId=" + instance.second.savesComponent)
        }

        @Throws(FileLoadException::class)
        private fun getModsComponent(instance: Pair<ComponentManifest, LauncherInstanceDetails>, files: LauncherFiles): Pair<ComponentManifest, LauncherModsDetails>? {
            var modsComponent: Pair<ComponentManifest, LauncherModsDetails>? = null
            if (instance.second.modsComponent?.isNotBlank() == true) {
                for (m in files.modsComponents) {
                    if (m.first.id == instance.second.modsComponent) {
                        modsComponent = m
                        break
                    }
                }
                modsComponent ?: throw FileLoadException("Failed to load instance data: unable to find mods component: modsId=" + instance.second.modsComponent)
            }
            return modsComponent
        }
    }
}
