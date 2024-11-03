package dev.treset.treelauncher.backend.data.manifest

import androidx.compose.runtime.*
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.LauncherFeature
import dev.treset.treelauncher.backend.data.LauncherFiles
import dev.treset.treelauncher.backend.data.LauncherLaunchArgument
import dev.treset.treelauncher.backend.launching.resources.InstanceResourceProvider
import dev.treset.treelauncher.backend.util.copyTo
import dev.treset.treelauncher.backend.util.exception.FileLoadException
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import dev.treset.treelauncher.backend.util.serialization.MutableDataStateList
import dev.treset.treelauncher.util.ListDisplay
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
class InstanceComponent(
    override val id: MutableDataState<String>,
    override val name: MutableDataState<String>,
    @JsonNames("version_component")
    val versionId: MutableDataState<String> = mutableStateOf(""),
    @JsonNames("saves_component")
    val savesId: MutableDataState<String> = mutableStateOf(""),
    @JsonNames("resourcepacks_component")
    val resourcepacksId: MutableDataState<String> = mutableStateOf(""),
    @JsonNames("options_component")
    val optionsId: MutableDataState<String> = mutableStateOf(""),
    @JsonNames("mods_component")
    val modsId: MutableDataState<String?> = mutableStateOf(null),
    @Transient override val file: MutableDataState<LauncherFile> = mutableStateOf(LauncherFile.of("")),
    override val active: MutableDataState<Boolean> = mutableStateOf(false),
    override val lastUsed: MutableDataState<String> = mutableStateOf(""),
    override val includedFiles: MutableDataStateList<String> = appConfig().instanceDefaultIncludedFiles.toMutableStateList(),
    val features: MutableDataStateList<LauncherFeature> = appConfig().instanceDefaultFeatures.toMutableStateList(),
    val jvmArguments: MutableDataStateList<LauncherLaunchArgument> = appConfig().instanceDefaultJvmArguments.toMutableStateList(),
    val ignoredFiles: MutableDataStateList<String> = appConfig().instanceDefaultIgnoredFiles.toMutableStateList(),
    val totalTime: MutableDataState<Long> = mutableStateOf(0),
    override val listDisplay: MutableDataState<ListDisplay?> = mutableStateOf(null)
): Component() {
    override val type = LauncherManifestType.INSTANCE_COMPONENT
    @Transient override var expectedType = LauncherManifestType.INSTANCE_COMPONENT

    @Transient val versionComponents = derivedStateOf { getVersionComponents(versionId.value, AppContext.files) }
    @Transient val javaComponent = derivedStateOf { getJavaComponent(versionComponents.value, AppContext.files) }
    @Transient val savesComponent = derivedStateOf { getSavesComponent(savesId.value, AppContext.files) }
    @Transient val resourcepacksComponent = derivedStateOf { getResourcepacksComponent(resourcepacksId.value, AppContext.files) }
    @Transient val optionsComponent = derivedStateOf { getOptionsComponent(optionsId.value, AppContext.files) }
    @Transient val modsComponent = derivedStateOf { getModsComponent(modsId.value, AppContext.files) }

    val librariesDir: LauncherFile
        get() = AppContext.files.librariesDir

    val gameDataDir: LauncherFile
        get() = AppContext.files.gameDataDir

    val assetsDir: LauncherFile
        get() = AppContext.files.assetsDir

    constructor(
        id: String,
        name: String,
        versionComponent: String,
        savesComponent: String,
        resourcepacksComponent: String,
        optionsComponent: String,
        modsComponent: String?,
        file: LauncherFile,
        active: Boolean = false,
        lastUsed: String = "",
        includedFiles: List<String> = appConfig().instanceDefaultIncludedFiles,
        features: List<LauncherFeature> = appConfig().instanceDefaultFeatures,
        jvmArguments: List<LauncherLaunchArgument> = appConfig().instanceDefaultJvmArguments,
        ignoredFiles: List<String> = appConfig().instanceDefaultIgnoredFiles,
        totalTime: Long = 0
    ): this(
        mutableStateOf(id),
        mutableStateOf(name),
        mutableStateOf(versionComponent),
        mutableStateOf(savesComponent),
        mutableStateOf(resourcepacksComponent),
        mutableStateOf(optionsComponent),
        mutableStateOf(modsComponent),
        mutableStateOf(file),
        mutableStateOf(active),
        mutableStateOf(lastUsed),
        includedFiles.toMutableStateList(),
        features.toMutableStateList(),
        jvmArguments.toMutableStateList(),
        ignoredFiles.toMutableStateList(),
        mutableStateOf(totalTime)
    )

    override fun getResourceProvider(gameDataDir: LauncherFile): InstanceResourceProvider {
        return InstanceResourceProvider(this, gameDataDir)
    }

    override fun copyData(other: Component) {
        super.copyData(other)

        if(other is InstanceComponent) {
            features.copyTo(other.features)
            ignoredFiles.copyTo(other.ignoredFiles)
            jvmArguments.copyTo(other.jvmArguments)
            other.modsId.value = modsId.value
            other.optionsId.value = optionsId.value
            other.resourcepacksId.value = resourcepacksId.value
            other.savesId.value = savesId.value
            other.versionId.value = versionId.value
            other.totalTime.value = totalTime.value
        }
    }

    companion object {
        @Throws(FileLoadException::class)
        fun getVersionComponents(id: String, files: LauncherFiles): List<VersionComponent> {
            val versionComponents: MutableList<VersionComponent> = mutableListOf()

            var firstComponent: VersionComponent? = null
            for (v in files.versionComponents) {
                if (v.id.value == id) {
                    firstComponent = v
                    break
                }
            }
            firstComponent?: throw FileLoadException("Failed to load instance data: unable to find version component: versionId=${id}")
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
        fun getOptionsComponent(id: String, files: LauncherFiles): OptionsComponent {
            for (o in files.optionsComponents) {
                if (o.id.value == id) {
                    return o
                }
            }
            throw FileLoadException("Failed to load instance data: unable to find options component: optionsId=$id")
        }

        @Throws(FileLoadException::class)
        fun getResourcepacksComponent(id: String, files: LauncherFiles): ResourcepackComponent {
            for (r in files.resourcepackComponents) {
                if (r.id.value == id) {
                    return r
                }
            }
            throw FileLoadException("Failed to load instance data: unable to find resourcepacks component: resourcepacksId=$id")
        }

        @Throws(FileLoadException::class)
        fun getSavesComponent(id: String, files: LauncherFiles): SavesComponent {
            for (s in files.savesComponents) {
                if (s.id.value == id) {
                    return s
                }
            }
            throw FileLoadException("Failed to load instance data: unable to find saves component: savesId=$id")
        }

        @Throws(FileLoadException::class)
        fun getModsComponent(id: String?, files: LauncherFiles): ModsComponent? {
            if (!id.isNullOrBlank()) {
                for (m in files.modsComponents) {
                    if (m.id.value == id) {
                        return m
                    }
                }
                throw FileLoadException("Failed to load instance data: unable to find mods component: modsId=$id")
            }
            return null
        }
    }
}