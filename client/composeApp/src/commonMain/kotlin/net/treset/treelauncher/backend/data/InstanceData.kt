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
    var versionComponents: List<Pair<LauncherManifest, LauncherVersionDetails>?>,
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
    var gameDataExcludedFiles: List<PatternString>
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
        files.getInstanceManifest()?.let {
            it.components?.let {comp ->
                if (!comp.remove(instance.first.id)) {
                    throw IOException("Unable to delete instance: unable to remove instance from launcher manifest")
                }
                try {
                    LauncherFile.of(it.directory, appConfig().MANIFEST_FILE_NAME).write(it)
                } catch (e: IOException) {
                    throw IOException("Unable to delete instance: unable to write launcher manifest", e)
                }
            }
        }?: throw IOException("Unable to delete instance: unable to load launcher manifest")
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
            files.getVersionComponents()?.let {
                var firstComponent: Pair<LauncherManifest, LauncherVersionDetails>? = null
                for (v in it) {
                    if (v?.first?.id == instance.second.versionComponent) {
                        firstComponent = v
                        break
                    }
                }
                firstComponent ?: throw FileLoadException("Failed to load instance data: unable to find version component: versionId=${instance.second.versionComponent}")
                var currentComponent: Pair<LauncherManifest, LauncherVersionDetails> = firstComponent
                versionComponents.add(currentComponent)
                while (currentComponent.second.depends != null && currentComponent.second.getDepends().isNotBlank()
                ) {
                    var found = false
                    for (v in it) {
                        if (v?.first?.id == currentComponent.second.depends) {
                            currentComponent = v!!
                            found = true
                            break
                        }
                    }
                    if (!found) {
                        throw FileLoadException("Failed to load instance data: unable to find dependent version component: versionId=${currentComponent.second.depends}")
                    }
                    versionComponents.add(currentComponent)
                }
            }?: throw FileLoadException("Failed to load instance data: unable to find version components")

            var javaComponent: LauncherManifest? = null
            files.getJavaComponents()?.let {
                for (v in versionComponents) {
                    if (v.second.java != null && v.second.java.isNotBlank()) {
                        for (j in it) {
                            if (j?.id == v.second.java) {
                                javaComponent = j
                                break
                            }
                        }
                        break
                    }
                }
                javaComponent ?: throw FileLoadException("Failed to load instance data: unable to find suitable java component")
            }?: throw FileLoadException("Failed to load instance data: unable to find java components")

            var optionsComponent: LauncherManifest? = null
            files.getOptionsComponents()?.let {
                for (o in it) {
                    if (o?.id == instance.second.optionsComponent) {
                        optionsComponent = o
                        break
                    }
                }
                optionsComponent
                    ?: throw FileLoadException("Failed to load instance data: unable to find options component: optionsId=" + instance.second.optionsComponent)
            }?: throw FileLoadException("Failed to load instance data: unable to find options components")

            var resourcepacksComponent: LauncherManifest? = null
            files.getResourcepackComponents()?.let {
                for (r in it) {
                    if (r?.id == instance.second.resourcepacksComponent) {
                        resourcepacksComponent = r
                        break
                    }
                }
                resourcepacksComponent
                    ?: throw FileLoadException("Failed to load instance data: unable to find resourcepacks component: resourcepacksId=" + instance.second.resourcepacksComponent)
            }?: throw FileLoadException("Failed to load instance data: unable to find resourcepacks components")

            var savesComponent: LauncherManifest? = null
            files.getSavesComponents()?.let {
                for (s in it) {
                    if (s?.id == instance.second.savesComponent) {
                        savesComponent = s
                        break
                    }
                }
                savesComponent
                    ?: throw FileLoadException("Failed to load instance data: unable to find saves component: savesId=" + instance.second.savesComponent)
            }?: throw FileLoadException("Failed to load instance data: unable to find saves components")

            var modsComponent: Pair<LauncherManifest, LauncherModsDetails>? = null
            if (instance.second.modsComponent != null && instance.second.modsComponent.isNotBlank()) {
                files.getModsComponents()?.let {
                    for (m in it) {
                        if (m?.first?.id == instance.second.modsComponent) {
                            modsComponent = m
                            break
                        }
                    }
                    modsComponent ?: throw FileLoadException("Failed to load instance data: unable to find mods component: modsId=" + instance.second.modsComponent)
                }?: throw FileLoadException("Failed to load instance data: unable to find mods components")
            }

            val gameDataExcludedFiles: ArrayList<PatternString> = ArrayList()
            files.getGameDetailsManifest()?.let {
                for (c in it.components) {
                    gameDataExcludedFiles.add(PatternString(c))
                }
            }
            gameDataExcludedFiles.add(PatternString(files.getModsManifest()?.prefix + ".*"))
            gameDataExcludedFiles.add(PatternString(files.getSavesManifest()?.prefix + ".*"))

            files.getLauncherDetails()?.let {
                return InstanceData(
                    it,
                    LauncherFile.of(
                        files.getMainManifest()?.directory ?: throw FileLoadException("Failed to load instance data: unable to load main manifest"),
                        files.getMainManifest()?.details ?: throw FileLoadException("Failed to load instance data: unable to load main manifest")
                    ),
                    instance,
                    versionComponents,
                    javaComponent?: throw FileLoadException("Failed to load instance data: unable to load java component"),
                    optionsComponent?: throw FileLoadException("Failed to load instance data: unable to load options component"),
                    resourcepacksComponent?: throw FileLoadException("Failed to load instance data: unable to load resourcepacks component"),
                    savesComponent?: throw FileLoadException("Failed to load instance data: unable to load saves component"),
                    modsComponent,
                    LauncherFile.ofRelative(it.gamedataDir),
                    LauncherFile.ofRelative(it.assetsDir),
                    LauncherFile.ofRelative(it.librariesDir),
                    files.getModsManifest()?.prefix ?: throw FileLoadException("Failed to load instance data: unable to load mods manifest"),
                    files.getSavesManifest()?.prefix ?: throw FileLoadException("Failed to load instance data: unable to load saves manifest"),
                    gameDataExcludedFiles
                )
            }?: throw FileLoadException("Failed to load instance data: unable to load launcher details")
        }
    }
}
