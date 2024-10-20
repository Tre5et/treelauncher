package dev.treset.treelauncher.backend.data.manifest

import androidx.compose.runtime.mutableStateOf
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class MainManifest(
    val activeInstance: MutableDataState<String?> = mutableStateOf(null),
    val assetsDir: MutableDataState<String>,
    val librariesDir: MutableDataState<String>,
    val gameDataDir: MutableDataState<String>,
    val instancesDir: MutableDataState<String>,
    val savesDir: MutableDataState<String>,
    val resourcepacksDir: MutableDataState<String>,
    val optionsDir: MutableDataState<String>,
    val modsDir: MutableDataState<String>,
    val versionDir: MutableDataState<String>,
    val javasDir: MutableDataState<String>,
    @Transient override val file: MutableDataState<LauncherFile> = mutableStateOf(LauncherFile.of(""))
): Manifest() {
    override val type = LauncherManifestType.LAUNCHER
    @Transient override var expectedType = LauncherManifestType.LAUNCHER

    constructor(
        activeInstance: String?,
        assetsDir: String,
        librariesDir: String,
        gameDataDir: String,
        instancesDir: String,
        savesDir: String,
        resourcepacksDir: String,
        optionsDir: String,
        modsDir: String,
        versionDir: String,
        javasDir: String,
        file: LauncherFile
    ): this(
        mutableStateOf(activeInstance),
        mutableStateOf(assetsDir),
        mutableStateOf(librariesDir),
        mutableStateOf(gameDataDir),
        mutableStateOf(instancesDir),
        mutableStateOf(savesDir),
        mutableStateOf(resourcepacksDir),
        mutableStateOf(optionsDir),
        mutableStateOf(modsDir),
        mutableStateOf(versionDir),
        mutableStateOf(javasDir),
        mutableStateOf(file)
    )

    fun copyTo(other: MainManifest) {
        other.activeInstance.value = activeInstance.value
        other.assetsDir.value = assetsDir.value
        other.librariesDir.value = librariesDir.value
        other.gameDataDir.value = gameDataDir.value
        other.instancesDir.value = instancesDir.value
        other.savesDir.value = savesDir.value
        other.resourcepacksDir.value = resourcepacksDir.value
        other.optionsDir.value = optionsDir.value
        other.modsDir.value = modsDir.value
        other.versionDir.value = versionDir.value
        other.javasDir.value = javasDir.value
        other.file.value = file.value
    }
}