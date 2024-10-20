package dev.treset.treelauncher.backend.data.manifest

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.LauncherLaunchArgument
import dev.treset.treelauncher.backend.util.copyTo
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import dev.treset.treelauncher.backend.util.serialization.MutableDataStateList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class VersionComponent(
    override val id: MutableDataState<String>,
    override val name: MutableDataState<String>,
    val versionNumber: MutableDataState<String>,
    val versionType: MutableDataState<String>,
    val loaderVersion: MutableDataState<String?> = mutableStateOf(null),
    val assets: MutableDataState<String?> = mutableStateOf(null),
    val virtualAssets: MutableDataState<String?> = mutableStateOf(null),
    val natives: MutableDataState<String?> = mutableStateOf(null),
    val depends: MutableDataState<String?> = mutableStateOf(null),
    val gameArguments: MutableDataStateList<LauncherLaunchArgument>,
    val jvmArguments: MutableDataStateList<LauncherLaunchArgument>,
    val java: MutableDataState<String?> = mutableStateOf(null),
    val libraries: MutableDataStateList<String>,
    val mainClass: MutableDataState<String>,
    val mainFile: MutableDataState<String?> = mutableStateOf(null),
    val versionId: MutableDataState<String>,
    @Transient override val file: MutableDataState<LauncherFile> = mutableStateOf(LauncherFile.of("")),
    override val includedFiles: MutableDataStateList<String> = appConfig().versionDefaultIncludedFiles.toMutableStateList(),
    override val lastUsed: MutableDataState<String> = mutableStateOf(""),
    override val active: MutableDataState<Boolean> = mutableStateOf(false)
): Component() {
    override val type = LauncherManifestType.VERSION_COMPONENT
    @Transient override var expectedType = LauncherManifestType.VERSION_COMPONENT

    constructor(
        id: String,
        name: String,
        versionNumber: String,
        versionType: String,
        loaderVersion: String?,
        assets: String?,
        virtualAssets: String?,
        natives: String?,
        depends: String?,
        gameArguments: List<LauncherLaunchArgument>,
        jvmArguments: List<LauncherLaunchArgument>,
        java: String?,
        libraries: List<String>,
        mainClass: String,
        mainFile: String?,
        versionId: String,
        file: LauncherFile,
        active: Boolean = false,
        lastUsed: String = "",
        includedFiles: List<String> = appConfig().versionDefaultIncludedFiles
    ): this(
        mutableStateOf(id),
        mutableStateOf(name),
        mutableStateOf(versionNumber),
        mutableStateOf(versionType),
        mutableStateOf(loaderVersion),
        mutableStateOf(assets),
        mutableStateOf(virtualAssets),
        mutableStateOf(natives),
        mutableStateOf(depends),
        gameArguments.toMutableStateList(),
        jvmArguments.toMutableStateList(),
        mutableStateOf(java),
        libraries.toMutableStateList(),
        mutableStateOf(mainClass),
        mutableStateOf(mainFile),
        mutableStateOf(versionId),
        mutableStateOf(file),
        includedFiles.toMutableStateList(),
        mutableStateOf(lastUsed),
        mutableStateOf(active)
    )

    override fun copyData(other: Component) {
        super.copyData(other)

        if (other is VersionComponent) {
            other.versionNumber.value = versionNumber.value
            other.versionType.value = versionType.value
            other.loaderVersion.value = loaderVersion.value
            other.assets.value = assets.value
            other.virtualAssets.value = virtualAssets.value
            other.natives.value = natives.value
            other.depends.value = depends.value
            gameArguments.copyTo(other.gameArguments)
            jvmArguments.copyTo(other.jvmArguments)
            other.java.value = java.value
            libraries.copyTo(other.libraries)
            other.mainClass.value = mainClass.value
        }
    }
}