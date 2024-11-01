package dev.treset.treelauncher.backend.launching.resources

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import dev.treset.mcdl.resourcepacks.Resourcepack
import dev.treset.mcdl.resourcepacks.Texturepack
import dev.treset.treelauncher.backend.util.MutableStateMap
import dev.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

data class ResourcepacksDisplayData(
    val resourcepacks: MutableStateMap<Resourcepack, LauncherFile>,
    val texturepacks: MutableStateMap<Texturepack, LauncherFile>,
    val onAddResourcepack: MutableState<ResourcepacksDisplayData.(source: List<LauncherFile>) -> Unit>,
    val onAddTexturepack: MutableState<ResourcepacksDisplayData.(source: List<LauncherFile>) -> Unit>
) {
    constructor() : this(
        mutableStateMapOf(),
        mutableStateMapOf(),
        mutableStateOf({ }),
        mutableStateOf({ })
    )

    @Throws(IOException::class)
    fun addResourcepacks(source: List<LauncherFile>) {
        (onAddResourcepack.value)(source)
    }

    @Throws(IOException::class)
    fun addTexturepacks(source: List<LauncherFile>) {
        (onAddTexturepack.value)(source)
    }
}