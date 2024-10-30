package dev.treset.treelauncher.backend.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import dev.treset.treelauncher.backend.data.manifest.*
import dev.treset.treelauncher.backend.util.exception.FileLoadException
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.MutableStateList

class InstanceData(
    val mainManifest: MainManifest,
    val instance: InstanceComponent,
    versionComponents: List<VersionComponent>,
    javaComponent: JavaComponent,
    optionsComponent: OptionsComponent,
    resourcepacksComponent: ResourcepackComponent,
    savesComponent: SavesComponent,
    modsComponent: ModsComponent?,
    val gameDataDir: LauncherFile,
    val assetsDir: LauncherFile,
    val librariesDir: LauncherFile
) {
    val versionComponents: MutableStateList<VersionComponent> = versionComponents.toMutableStateList()
    val javaComponent: MutableState<JavaComponent> = mutableStateOf(javaComponent)
    val optionsComponent: MutableState<OptionsComponent> = mutableStateOf(optionsComponent)
    val resourcepacksComponent: MutableState<ResourcepackComponent> = mutableStateOf(resourcepacksComponent)
    val savesComponent: MutableState<SavesComponent> = mutableStateOf(savesComponent)
    val modsComponent: MutableState<ModsComponent?> = mutableStateOf(modsComponent)

    companion object {
        @Throws(FileLoadException::class)
        fun of(instance: InstanceComponent, files: LauncherFiles): InstanceData {
            val versions = getVersionComponents(instance, files)
            return InstanceData(
                files.mainManifest,
                instance,
                versions,
                getJavaComponent(versions, files),
                getOptionsComponent(instance, files),
                getResourcepacksComponent(instance, files),
                getSavesComponent(instance, files),
                getModsComponent(instance, files),
                files.gameDataDir,
                files.assetsDir,
                files.librariesDir
            )
        }

        @Throws(FileLoadException::class)
        fun getVersionComponents(instance: InstanceComponent, files: LauncherFiles): List<VersionComponent> {
            val versionComponents: MutableList<VersionComponent> = mutableListOf()

            var firstComponent: VersionComponent? = null
            for (v in files.versionComponents) {
                if (v.id.value == instance.versionComponent.value) {
                    firstComponent = v
                    break
                }
            }
            firstComponent?: throw FileLoadException("Failed to load instance data: unable to find version component: versionId=${instance.versionComponent.value}")
            var currentComponent: VersionComponent = firstComponent
            versionComponents.add(currentComponent)
            while (!currentComponent.depends.value.isNullOrBlank()) {
                var found = false
                for (v in files.versionComponents) {
                    if (v.id.value == currentComponent.depends.value) {
                        currentComponent = v
                        found = true
                        break
                    }
                }
                if (!found) {
                    throw FileLoadException("Failed to load instance data: unable to find dependent version component: versionId=${currentComponent.depends.value}")
                }
                versionComponents.add(currentComponent)
            }

            return versionComponents
        }

        @Throws(FileLoadException::class)
        fun getJavaComponent(versionComponents: List<VersionComponent>, files: LauncherFiles): JavaComponent {
            for (v in versionComponents) {
                if (!v.java.value.isNullOrBlank()) {
                    for (j in files.javaComponents) {
                        if (j.id.value == v.java.value) {
                            return j
                        }
                    }
                    throw FileLoadException("Failed to load instance data: unable to find java component: javaId=" + v.java.value)
                }
            }
            throw FileLoadException("Failed to load instance data: unable to find version with java component")
        }

        @Throws(FileLoadException::class)
        fun getOptionsComponent(instance: InstanceComponent, files: LauncherFiles): OptionsComponent {
            for (o in files.optionsComponents) {
                if (o.id.value == instance.optionsComponent.value) {
                    return o
                }
            }
            throw FileLoadException("Failed to load instance data: unable to find options component: optionsId=" + instance.optionsComponent.value)
        }

        @Throws(FileLoadException::class)
        fun getResourcepacksComponent(instance: InstanceComponent, files: LauncherFiles): ResourcepackComponent {
            for (r in files.resourcepackComponents) {
                if (r.id.value == instance.resourcepacksComponent.value) {
                    return r
                }
            }
            throw FileLoadException("Failed to load instance data: unable to find resourcepacks component: resourcepacksId=" + instance.resourcepacksComponent.value)
        }

        @Throws(FileLoadException::class)
        fun getSavesComponent(instance: InstanceComponent, files: LauncherFiles): SavesComponent {
            for (s in files.savesComponents) {
                if (s.id.value == instance.savesComponent.value) {
                    return s
                }
            }
            throw FileLoadException("Failed to load instance data: unable to find saves component: savesId=" + instance.savesComponent.value)
        }

        @Throws(FileLoadException::class)
        fun getModsComponent(instance: InstanceComponent, files: LauncherFiles): ModsComponent? {
            if (!instance.modsComponent.value.isNullOrBlank()) {
                for (m in files.modsComponents) {
                    if (m.id.value == instance.modsComponent.value) {
                        return m
                    }
                }
                throw FileLoadException("Failed to load instance data: unable to find mods component: modsId=" + instance.modsComponent.value)
            }
            return null
        }
    }
}
