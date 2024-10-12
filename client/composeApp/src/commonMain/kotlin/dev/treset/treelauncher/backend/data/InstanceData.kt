package dev.treset.treelauncher.backend.data

import dev.treset.treelauncher.backend.data.manifest.*
import dev.treset.treelauncher.backend.util.exception.FileLoadException
import dev.treset.treelauncher.backend.util.file.LauncherFile

class InstanceData(
    var mainManifest: MainManifest,
    var instance: InstanceComponent,
    var versionComponents: Array<VersionComponent>,
    var javaComponent: JavaComponent,
    var optionsComponent: OptionsComponent,
    var resourcepacksComponent: ResourcepackComponent,
    var savesComponent: SavesComponent,
    var modsComponent: ModsComponent?,
    var gameDataDir: LauncherFile,
    var assetsDir: LauncherFile,
    var librariesDir: LauncherFile
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

    companion object {
        @Throws(FileLoadException::class)
        fun of(instance: InstanceComponent, files: LauncherFiles): InstanceData {
            val versionComponents = getVersionComponents(instance, files)
            val virtualDir = versionComponents.firstOrNull{it.virtualAssets != null}?.virtualAssets
            val assetsDir = virtualDir?.let {
                LauncherFile.ofData(files.mainManifest.assetsDir, it)
            } ?: LauncherFile.ofData(files.mainManifest.assetsDir)

            return InstanceData(
                files.mainManifest,
                instance,
                versionComponents,
                getJavaComponent(versionComponents, files),
                getOptionsComponent(instance, files),
                getResourcepacksComponent(instance, files),
                getSavesComponent(instance, files),
                getModsComponent(instance, files),
                LauncherFile.ofData(files.mainManifest.gameDataDir),
                assetsDir,
                LauncherFile.ofData(files.mainManifest.librariesDir)
            )
        }

        @Throws(FileLoadException::class)
        private fun getVersionComponents(instance: InstanceComponent, files: LauncherFiles): Array<VersionComponent> {
            val versionComponents: MutableList<VersionComponent> = mutableListOf()

            var firstComponent: VersionComponent? = null
            for (v in files.versionComponents) {
                if (v.id == instance.versionComponent) {
                    firstComponent = v
                    break
                }
            }
            firstComponent?: throw FileLoadException("Failed to load instance data: unable to find version component: versionId=${instance.versionComponent}")
            var currentComponent: VersionComponent = firstComponent
            versionComponents.add(currentComponent)
            while (currentComponent.depends?.isNotBlank() == true) {
                var found = false
                for (v in files.versionComponents) {
                    if (v.id == currentComponent.depends) {
                        currentComponent = v
                        found = true
                        break
                    }
                }
                if (!found) {
                    throw FileLoadException("Failed to load instance data: unable to find dependent version component: versionId=${currentComponent.depends}")
                }
                versionComponents.add(currentComponent)
            }

            return versionComponents.toTypedArray()
        }

        @Throws(FileLoadException::class)
        private fun getJavaComponent(versionComponents: Array<VersionComponent>, files: LauncherFiles): JavaComponent {
            for (v in versionComponents) {
                if (v.java?.isNotBlank() == true) {
                    for (j in files.javaComponents) {
                        if (j.id == v.java) {
                            return j
                        }
                    }
                    throw FileLoadException("Failed to load instance data: unable to find java component: javaId=" + v.java)
                }
            }
            throw FileLoadException("Failed to load instance data: unable to find version with java component")
        }

        @Throws(FileLoadException::class)
        private fun getOptionsComponent(instance: InstanceComponent, files: LauncherFiles): OptionsComponent {
            for (o in files.optionsComponents) {
                if (o.id == instance.optionsComponent) {
                    return o
                }
            }
            throw FileLoadException("Failed to load instance data: unable to find options component: optionsId=" + instance.optionsComponent)
        }

        @Throws(FileLoadException::class)
        private fun getResourcepacksComponent(instance: InstanceComponent, files: LauncherFiles): ResourcepackComponent {
            for (r in files.resourcepackComponents) {
                if (r.id == instance.resourcepacksComponent) {
                    return r
                }
            }
            throw FileLoadException("Failed to load instance data: unable to find resourcepacks component: resourcepacksId=" + instance.resourcepacksComponent)
        }

        @Throws(FileLoadException::class)
        private fun getSavesComponent(instance: InstanceComponent, files: LauncherFiles): SavesComponent {
            for (s in files.savesComponents) {
                if (s.id == instance.savesComponent) {
                    return s
                }
            }
            throw FileLoadException("Failed to load instance data: unable to find saves component: savesId=" + instance.savesComponent)
        }

        @Throws(FileLoadException::class)
        private fun getModsComponent(instance: InstanceComponent, files: LauncherFiles): ModsComponent? {
            if (instance.modsComponent?.isNotBlank() == true) {
                for (m in files.modsComponents) {
                    if (m.id == instance.modsComponent) {
                        return m
                    }
                }
                throw FileLoadException("Failed to load instance data: unable to find mods component: modsId=" + instance.modsComponent)
            }
            return null
        }
    }
}
