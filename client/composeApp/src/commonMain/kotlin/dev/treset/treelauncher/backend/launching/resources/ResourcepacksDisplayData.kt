package dev.treset.treelauncher.backend.launching.resources

import androidx.compose.runtime.mutableStateMapOf
import dev.treset.mcdl.resourcepacks.Resourcepack
import dev.treset.mcdl.resourcepacks.Texturepack
import dev.treset.treelauncher.backend.util.MutableStateMap
import dev.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

data class ResourcepacksDisplayData(
    val resourcepacks: MutableStateMap<Resourcepack, LauncherFile>,
    val texturepacks: MutableStateMap<Texturepack, LauncherFile>,
    private val onAddResourcepack: ResourcepacksDisplayData.(source: List<LauncherFile>) -> Unit,
    private val onAddTexturepack: ResourcepacksDisplayData.(source: List<LauncherFile>) -> Unit
) {
    constructor() : this(
        mutableStateMapOf(),
        mutableStateMapOf(),
        { },
        { }
    )

    @Throws(IOException::class)
    fun addResourcepacks(source: List<LauncherFile>) {
        onAddResourcepack(source)
    }

    @Throws(IOException::class)
    fun addTexturepacks(source: List<LauncherFile>) {
        onAddTexturepack(source)
    }
}