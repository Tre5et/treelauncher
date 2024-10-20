package dev.treset.treelauncher.backend.data.manifest

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.LauncherFeature
import dev.treset.treelauncher.backend.data.LauncherLaunchArgument
import dev.treset.treelauncher.backend.launching.resources.InstanceResourceProvider
import dev.treset.treelauncher.backend.util.copyTo
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import dev.treset.treelauncher.backend.util.serialization.MutableDataStateList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class InstanceComponent(
    override val id: MutableDataState<String>,
    override val name: MutableDataState<String>,
    val versionComponent: MutableDataState<String> = mutableStateOf(""),
    val savesComponent: MutableDataState<String> = mutableStateOf(""),
    val resourcepacksComponent: MutableDataState<String> = mutableStateOf(""),
    val optionsComponent: MutableDataState<String> = mutableStateOf(""),
    val modsComponent: MutableDataState<String?> = mutableStateOf(null),
    @Transient override val file: MutableDataState<LauncherFile> = mutableStateOf(LauncherFile.of("")),
    override val active: MutableDataState<Boolean> = mutableStateOf(false),
    override val lastUsed: MutableDataState<String> = mutableStateOf(""),
    override val includedFiles: MutableDataStateList<String> = appConfig().instanceDefaultIncludedFiles.toMutableStateList(),
    val features: MutableDataStateList<LauncherFeature> = appConfig().instanceDefaultFeatures.toMutableStateList(),
    val jvmArguments: MutableDataStateList<LauncherLaunchArgument> = appConfig().instanceDefaultJvmArguments.toMutableStateList(),
    val ignoredFiles: MutableDataStateList<String> = appConfig().instanceDefaultIgnoredFiles.toMutableStateList(),
    val totalTime: MutableDataState<Long> = mutableStateOf(0)
): Component() {
    override val type = LauncherManifestType.INSTANCE_COMPONENT
    @Transient override var expectedType = LauncherManifestType.INSTANCE_COMPONENT

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
            other.modsComponent.value = modsComponent.value
            other.optionsComponent.value = optionsComponent.value
            other.resourcepacksComponent.value = resourcepacksComponent.value
            other.savesComponent.value = savesComponent.value
            other.versionComponent.value = versionComponent.value
            other.totalTime.value = totalTime.value
        }
    }
}