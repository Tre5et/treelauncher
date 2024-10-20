package dev.treset.treelauncher.backend.data.manifest

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.util.copyTo
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import dev.treset.treelauncher.backend.util.serialization.MutableDataStateList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class ModsComponent(
    override val id: MutableDataState<String>,
    override val name: MutableDataState<String>,
    val types: MutableDataStateList<String>,
    val versions: MutableDataStateList<String>,
    @Transient override val file: MutableDataState<LauncherFile> = mutableStateOf(LauncherFile.of("")),
    override val includedFiles: MutableDataStateList<String> = appConfig().modsDefaultIncludedFiles.toMutableStateList(),
    override val lastUsed: MutableDataState<String> = mutableStateOf(""),
    override val active: MutableDataState<Boolean> = mutableStateOf(false),
    val mods: MutableDataStateList<LauncherMod> = mutableStateListOf()
): Component() {
    override val type = LauncherManifestType.MODS_COMPONENT
    @Transient override var expectedType = LauncherManifestType.MODS_COMPONENT
    
    constructor(
        id: String,
        name: String,
        types: List<String>,
        versions: List<String>,
        file: LauncherFile,
        active: Boolean = false,
        lastUsed: String = "",
        includedFiles: List<String> = appConfig().modsDefaultIncludedFiles,
        mods: List<LauncherMod> = emptyList()
    ): this(
        mutableStateOf(id),
        mutableStateOf(name),
        types.toMutableStateList(),
        versions.toMutableStateList(),
        mutableStateOf(file),
        includedFiles.toMutableStateList(),
        mutableStateOf(lastUsed),
        mutableStateOf(active),
        mods.toMutableStateList()
    )

    override fun copyData(other: Component) {
        super.copyData(other)

        if (other is ModsComponent) {
            types.copyTo(other.types)
            versions.copyTo(other.versions)
            mods.copyTo(other.mods)
        }
    }
}