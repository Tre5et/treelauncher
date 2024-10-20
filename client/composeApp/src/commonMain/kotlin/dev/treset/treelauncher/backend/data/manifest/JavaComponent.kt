package dev.treset.treelauncher.backend.data.manifest

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import dev.treset.treelauncher.backend.util.serialization.MutableDataStateList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class JavaComponent(
    override val id: MutableDataState<String>,
    override val name: MutableDataState<String>,
    @Transient override val file: MutableDataState<LauncherFile> = mutableStateOf(LauncherFile.of("")),
    override val includedFiles: MutableDataStateList<String> = appConfig().javaDefaultIncludedFiles.toMutableStateList(),
    override val lastUsed: MutableDataState<String> = mutableStateOf(""),
    override val active: MutableDataState<Boolean> = mutableStateOf(false)
): Component() {
    override val type = LauncherManifestType.JAVA_COMPONENT
    @Transient override var expectedType = LauncherManifestType.JAVA_COMPONENT

    constructor(
        id: String,
        name: String,
        file: LauncherFile,
        active: Boolean = false,
        lastUsed: String = "",
        includedFiles: List<String> = appConfig().javaDefaultIncludedFiles
    ): this(
        mutableStateOf(id),
        mutableStateOf(name),
        mutableStateOf(file),
        includedFiles.toMutableStateList(),
        mutableStateOf(lastUsed),
        mutableStateOf(active)
    )
}