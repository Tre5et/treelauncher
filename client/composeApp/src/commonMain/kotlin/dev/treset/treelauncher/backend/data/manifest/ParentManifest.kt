package dev.treset.treelauncher.backend.data.manifest

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import dev.treset.treelauncher.backend.util.copyTo
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import dev.treset.treelauncher.backend.util.serialization.MutableDataStateList
import dev.treset.treelauncher.backend.util.sort.ComponentSortType
import dev.treset.treelauncher.backend.util.sort.Sort
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
open class ParentManifest(
    override val type: LauncherManifestType,
    val prefix: MutableDataState<String>,
    val components: MutableDataStateList<String>,
    @Transient override var file: MutableState<LauncherFile> = mutableStateOf(LauncherFile.of("")),
    val sort: Sort<Component> = Sort(
        ComponentSortType.NAME.comparator,
        false
    )
): Manifest() {
    @Transient override var expectedType = LauncherManifestType.UNKNOWN

    constructor(
        type: LauncherManifestType,
        prefix: String,
        components: List<String>,
        file: LauncherFile
    ): this(
        type,
        mutableStateOf(prefix),
        components.toMutableStateList(),
        mutableStateOf(file)
    )

    fun copyTo(other: ParentManifest) {
        other.prefix.value = prefix.value
        components.copyTo(other.components)
        other.file = file
    }
}